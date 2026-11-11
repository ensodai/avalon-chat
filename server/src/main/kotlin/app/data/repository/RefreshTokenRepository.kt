package app.data.repository


//class RefreshTokenRepository {
//
//    /**
//     * Находит refresh-токен в базе данных по его значению.
//     *
//     * @param tokenValue Строковое значение токена.
//     * @return DAO-сущность [RefreshToken] если токен найден, иначе `null`.
//     */
//    suspend fun findByToken(tokenValue: String): RefreshToken? {
//        return dbQuery {
//            RefreshToken.findById(tokenValue)
//        }
//    }
//
//    /**
//     * Отзывает (делает недействительным) указанный refresh-токен.
//     *
//     * @param tokenValue Строковое значение токена, который необходимо отозвать.
//     */
//    suspend fun revokeToken(tokenValue: String) {
//        dbQuery {
//            RefreshToken.findById(tokenValue)?.revoked = true
//        }
//    }
//
//    /**
//     * Сохраняет новый refresh-токен в базе данных.
//     *
//     * @param tokenValue Уникальное строковое значение токена.
//     * @param userId UUID пользователя, которому принадлежит токен.
//     * @param expiresAt Время истечения срока действия токена в миллисекундах (epoch).
//     */
//    suspend fun saveRefreshToken(tokenValue: String, userId: UUID, expiresAt: Long) {
//        dbQuery {
//            RefreshToken.new(tokenValue) {
//                this.userId = EntityID(userId, AppUsers)
//                this.expiresAt = expiresAt
//                this.revoked = false
//            }
//        }
//    }
//}