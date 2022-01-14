package ai.beans.common.widgets.autocomplete

import ai.beans.common.R
import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.RelativeLayout

class BeansAutocompleteWidget : RelativeLayout {

    var beansAutocompleteTextView : AutoCompleteTextView ?= null
    var adapter: ArrayAdapter<*> ?= null
    var widgetListener: AutocompleteWidgetListener ?= null
    var showDefaultValues: Boolean = false
    var clearBtn: RelativeLayout ?= null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        //setupContent()
    }

     init {
         val v=LayoutInflater.from(context).inflate(R.layout.auto_complete_widget, this, true)
         beansAutocompleteTextView = v.findViewById(R.id.autocomplete_textView)
         clearBtn = v.findViewById(R.id.clearBtn)

         clearBtn?.setOnClickListener {
            setText("")
         }
    }

    fun getCurrentText() : String? {
        if(beansAutocompleteTextView != null) {
            return beansAutocompleteTextView!!.text.toString()
        }
        return null
    }

    fun setHintText(hint: String) {
        if(hint != null) {
            beansAutocompleteTextView?.hint = hint
        }
    }

    fun setText(text: String) {
        if(text != null) {
            beansAutocompleteTextView?.setText(text)
        }
    }

    fun clearText() {
        //Clear the adapter and the textView
    }

    @SuppressLint("ClickableViewAccessibility")
    fun <T : Any?> setup(suggestionsAdapter: ArrayAdapter<T>) {
        beansAutocompleteTextView?.threshold = 1
        beansAutocompleteTextView?.setAdapter(suggestionsAdapter)
        adapter = suggestionsAdapter!!


        beansAutocompleteTextView?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s != null && !s.isBlank()) {
                    clearBtn?.visibility = View.VISIBLE
                } else {
                    clearBtn?.visibility = View.GONE
                }
            }

        })

        beansAutocompleteTextView?.onItemClickListener = object: AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                widgetListener?.onItemSelected(position)
            }
        }

        beansAutocompleteTextView?.setOnTouchListener(object:View.OnTouchListener{
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if(showDefaultValues)
                    beansAutocompleteTextView?.showDropDown()
                return false
            }

        })

    }

    fun setFocus() {
        beansAutocompleteTextView?.requestFocusFromTouch()
    }

    fun showDefaultValues(flag : Boolean) {
        showDefaultValues = flag
    }
}