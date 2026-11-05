package com.sanjana.mornote

sealed class Screen(val route: String) {
    object NoteList : Screen("note_list")
    object Feed : Screen("feed")
    object Settings : Screen("settings")
    object Archive : Screen("archive")
    object NoteDetail : Screen("note_detail/{noteId}") {
        fun createRoute(noteId: Int) = route.replace("{noteId}", noteId.toString())
    }
}
