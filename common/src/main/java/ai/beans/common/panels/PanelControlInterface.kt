package ai.beans.common.panels

import ai.beans.common.ui.core.BeansFragment

interface PanelControlInterface {
    fun setOwnerFragment(fragment : BeansFragment)
    fun setPanelInteractionListener(listener : PanelInteractionListener)
    fun expand()
    fun collapse()
    fun hide()
}