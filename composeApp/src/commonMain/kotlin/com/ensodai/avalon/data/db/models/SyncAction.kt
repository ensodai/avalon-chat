package com.ensodai.avalon.data.db.models

enum class SyncAction {
    /**
     * Данные синхронизированы с сервером. Всё ок.
     */
    NONE,

    /**
     * Запись создана локально, но еще нет на сервере.
     * Действие при синке: POST /api/entity
     */
    CREATE,

    /**
     * Запись изменена локально (например, сменил название чата).
     * Действие при синке: PUT/PATCH /api/entity/{id}
     */
    UPDATE,

    /**
     * Запись удалена локально.
     * Действие при синке: DELETE /api/entity/{id}
     */
    DELETE
}