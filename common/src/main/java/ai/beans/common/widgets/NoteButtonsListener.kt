package ai.beans.common.widgets

import ai.beans.common.events.ShowDataEntryDialog

interface NoteButtonsListener {
    fun noteWidgetClicked(event: ShowDataEntryDialog)
}
