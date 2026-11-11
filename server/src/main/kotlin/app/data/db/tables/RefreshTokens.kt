package app.data.db.tables


//object RefreshTokens : IdTable<String>("refresh_tokens") {
//    override val id = varchar("token", 255).entityId()
//    val userId = reference("user_id", AppUsers, onDelete = ReferenceOption.CASCADE)
//    val expiresAt = long("expires_at")
//    val revoked = bool("revoked").default(false)
//
//    init {
//        uniqueIndex("token", id)
//    }
//}
//
//class RefreshToken(id: EntityID<String>) : Entity<String>(id) {
//    companion object : EntityClass<String, RefreshToken>(RefreshTokens)
//
//    var userId: EntityID<Uuid> by RefreshTokens.userId
//    var expiresAt: Long by RefreshTokens.expiresAt
//    var revoked: Boolean by RefreshTokens.revoked
//}
