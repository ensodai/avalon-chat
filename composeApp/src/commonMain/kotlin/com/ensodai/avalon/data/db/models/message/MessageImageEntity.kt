package com.ensodai.avalon.data.db.models.message

//@Entity(
//    tableName = "message_images",
//    foreignKeys = [ForeignKey(
//        entity = MessageEntity::class,
//        parentColumns = ["id"],
//        childColumns = ["message_id"],
//        onDelete = ForeignKey.CASCADE
//    )],
//    indices = [Index("message_id")]
//)
//data class MessageImageEntity(
//    @PrimaryKey(autoGenerate = true)
//    val id: Int = 0,
//    @ColumnInfo(name = "message_id")
//    val messageId: Int,
//    @ColumnInfo(name = "image_url")
//    val imageUrl: String
//)
//
//fun List<String>?.toMessageImages(messageId: Int): List<MessageImageEntity> {
//    return this?.map { imageUrl ->
//        MessageImageEntity(messageId = messageId, imageUrl = imageUrl)
//    } ?: emptyList()
//}
