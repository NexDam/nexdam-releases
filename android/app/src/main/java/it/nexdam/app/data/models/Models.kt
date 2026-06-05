package it.nexdam.app.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Project(
    val id: String,
    val title: String,
    val description: String? = null,
    val status: String = "waiting",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("project_messages") val messages: List<ProjectMessage> = emptyList(),
    @SerialName("project_files") val files: List<ProjectFile> = emptyList(),
    val invoices: List<Invoice> = emptyList()
)

@Serializable
data class ProjectMessage(
    val id: String,
    @SerialName("project_id") val projectId: String? = null,
    @SerialName("sender_id") val senderId: String? = null,
    @SerialName("is_admin") val isAdmin: Boolean = false,
    val body: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ProjectFile(
    val id: String,
    @SerialName("project_id") val projectId: String? = null,
    val name: String,
    val url: String? = null,
    @SerialName("size_label") val sizeLabel: String? = null,
    @SerialName("uploaded_by_admin") val uploadedByAdmin: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Invoice(
    val id: String,
    @SerialName("project_id") val projectId: String? = null,
    val amount: Double,
    val currency: String = "EUR",
    val status: String = "pending",
    val description: String? = null,
    @SerialName("due_date") val dueDate: String? = null,
    @SerialName("paid_at") val paidAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Profile(
    val id: String,
    val username: String? = null,
    @SerialName("full_name") val fullName: String? = null,
    val company: String? = null,
    val phone: String? = null,
    val role: String? = null,
    @SerialName("email_verified") val emailVerified: Boolean = false
)
