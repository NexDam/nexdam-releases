// Edge Function invocata da un Database Webhook su INSERT in `project_messages`.
// Quando il team NexDam (is_admin = true) scrive un messaggio, invia una push
// FCM (HTTP v1 API) a tutti i dispositivi registrati del cliente destinatario,
// così la notifica arriva anche ad app completamente chiusa.
//
// Secrets richiesti (da impostare con `supabase secrets set`):
//   SUPABASE_URL                 -> URL del progetto (già disponibile di default)
//   SUPABASE_SERVICE_ROLE_KEY    -> service role key (per leggere oltre le RLS)
//   FCM_PROJECT_ID               -> project_id del file service-account Firebase
//   FCM_CLIENT_EMAIL             -> client_email del file service-account Firebase
//   FCM_PRIVATE_KEY              -> private_key del file service-account Firebase
//                                   (con i \n letterali, vedi README)

import { create, getNumericDate } from "https://deno.land/x/djwt@v3.0.2/mod.ts";
import { createClient } from "https://esm.sh/@supabase/supabase-js@2";

const SUPABASE_URL = Deno.env.get("SUPABASE_URL")!;
const SERVICE_ROLE_KEY = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!;
const FCM_PROJECT_ID = Deno.env.get("FCM_PROJECT_ID")!;
const FCM_CLIENT_EMAIL = Deno.env.get("FCM_CLIENT_EMAIL")!;
const FCM_PRIVATE_KEY = Deno.env.get("FCM_PRIVATE_KEY")!.replace(/\\n/g, "\n");

const supabaseAdmin = createClient(SUPABASE_URL, SERVICE_ROLE_KEY);

async function getAccessToken(): Promise<string> {
    const key = await crypto.subtle.importKey(
        "pkcs8",
        pemToArrayBuffer(FCM_PRIVATE_KEY),
        { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
        false,
        ["sign"],
    );

    const jwt = await create(
        { alg: "RS256", typ: "JWT" },
        {
            iss: FCM_CLIENT_EMAIL,
            scope: "https://www.googleapis.com/auth/firebase.messaging",
            aud: "https://oauth2.googleapis.com/token",
            iat: getNumericDate(0),
            exp: getNumericDate(3600),
        },
        key,
    );

    const response = await fetch("https://oauth2.googleapis.com/token", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded" },
        body: new URLSearchParams({
            grant_type: "urn:ietf:params:oauth:grant-type:jwt-bearer",
            assertion: jwt,
        }),
    });

    if (!response.ok) {
        throw new Error(`Impossibile ottenere l'access token FCM: ${await response.text()}`);
    }

    const json = await response.json();
    return json.access_token as string;
}

function pemToArrayBuffer(pem: string): ArrayBuffer {
    const stripped = pem
        .replace(/-----BEGIN PRIVATE KEY-----/, "")
        .replace(/-----END PRIVATE KEY-----/, "")
        .replace(/\s+/g, "");
    const binary = atob(stripped);
    const bytes = new Uint8Array(binary.length);
    for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
    return bytes.buffer;
}

async function sendPush(accessToken: string, token: string, title: string, body: string) {
    const response = await fetch(
        `https://fcm.googleapis.com/v1/projects/${FCM_PROJECT_ID}/messages:send`,
        {
            method: "POST",
            headers: {
                Authorization: `Bearer ${accessToken}`,
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                message: {
                    token,
                    notification: { title, body },
                    data: { projectTitle: title, body },
                    android: { priority: "high" },
                },
            }),
        },
    );
    return response;
}

Deno.serve(async (req) => {
    try {
        const payload = await req.json();
        const record = payload.record ?? payload;

        const isAdmin = record.is_admin === true;
        const projectId = record.project_id as string | undefined;
        const body = record.body as string | undefined;
        const senderId = record.sender_id as string | undefined;

        if (!isAdmin || !projectId || !body) {
            return new Response(JSON.stringify({ skipped: true }), { status: 200 });
        }

        const { data: project, error: projectError } = await supabaseAdmin
            .from("projects")
            .select("title, client_id")
            .eq("id", projectId)
            .single();

        if (projectError || !project || project.client_id === senderId) {
            return new Response(JSON.stringify({ skipped: true }), { status: 200 });
        }

        const { data: tokens, error: tokensError } = await supabaseAdmin
            .from("device_push_tokens")
            .select("token")
            .eq("user_id", project.client_id);

        if (tokensError || !tokens || tokens.length === 0) {
            return new Response(JSON.stringify({ skipped: true, reason: "no_tokens" }), { status: 200 });
        }

        const accessToken = await getAccessToken();
        const title = `Nuovo messaggio · ${project.title}`;

        const results = await Promise.all(
            tokens.map(async ({ token }) => {
                const res = await sendPush(accessToken, token, title, body);
                if (!res.ok && (res.status === 404 || res.status === 410)) {
                    // token non più valido: rimuovilo
                    await supabaseAdmin.from("device_push_tokens").delete().eq("token", token);
                }
                return { token, ok: res.ok };
            }),
        );

        return new Response(JSON.stringify({ sent: results }), {
            status: 200,
            headers: { "Content-Type": "application/json" },
        });
    } catch (error) {
        console.error(error);
        return new Response(JSON.stringify({ error: String(error) }), { status: 500 });
    }
});
