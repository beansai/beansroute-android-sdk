package ai.beans.common.widgets.autocomplete

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.widget.AutoCompleteTextView

class BeansAutoCompleteTextView : AutoCompleteTextView {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
    }

    override fun enoughToFilter(): Boolean {
        return true
    }


}