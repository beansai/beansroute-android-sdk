package ai.beans.common.widgets

import ai.beans.common.R
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable

class CommonToolbar : LinearLayout {

    lateinit var title :TextView
    lateinit var minorTitle :TextView
    lateinit var leftHamburgerButton :ImageView
    lateinit var rightShareButton :ImageView
    lateinit var leftActionButton :TextView
    lateinit var rightActionButton :TextView
    lateinit var searchBoxContainer : RelativeLayout
    lateinit var regularModeContainer : RelativeLayout
    lateinit var backButton : ImageView
    lateinit var rightImageButton : ImageView

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.common_toolbar, this, true)
        tag = context.getString(R.string.common_tool_bar)
        setup()
    }

    private fun setup() {
        regularModeContainer = findViewById(R.id.regularModeContainer)
        searchBoxContainer = findViewById(R.id.searchBoxContainer)
        title = findViewById(R.id.text_action_bar)
        minorTitle= findViewById(R.id.minor_text_action_bar)
        leftActionButton = findViewById(R.id.leftToolbarButton)
        rightActionButton = findViewById(R.id.rightToolbarButton)
        backButton = findViewById(R.id.backButton)
        leftHamburgerButton = findViewById(R.id.hamburger)
        rightShareButton = findViewById(R.id.share)
        rightImageButton= findViewById(R.id.rightImageButton)
    }

    fun setTitle(title : String) {
        this.title?.text = title
    }

    fun showMinorTitleText(title : String) {
        this.minorTitle?.text = title
        this.minorTitle?.visibility = View.VISIBLE
    }

    fun hideMinorTitleText() {
        this.minorTitle?.visibility = View.GONE
    }

    fun setLeftButtonTitle(title: String, listener: OnClickListener?) {
        leftHamburgerButton.visibility = View.GONE
        backButton.visibility = View.GONE
        leftActionButton.visibility = View.VISIBLE
        leftActionButton.text = title
        leftActionButton.setOnClickListener(listener)
    }

    fun setLeftHamburgerButton(listener: OnClickListener?) {
        backButton.visibility = View.GONE
        leftActionButton.visibility = View.GONE
        leftHamburgerButton.visibility = View.VISIBLE
        leftHamburgerButton.setOnClickListener(listener)
    }

    fun setLeftBackButton(listener: OnClickListener?) {
        leftHamburgerButton.visibility = View.GONE
        leftActionButton.visibility = View.GONE
        backButton.visibility = View.VISIBLE
        backButton.setOnClickListener(listener)
    }


    fun setRightButtonTitle(title: String, listener: OnClickListener?) {
        rightActionButton.visibility = View.VISIBLE
        rightActionButton.text = title
        rightActionButton.setOnClickListener(listener)
    }

    fun setSearchToolBar(visibility : Boolean) {
        if(visibility) {
            regularModeContainer.visibility = View.GONE
            searchBoxContainer.visibility = View.VISIBLE
        }
    }

    fun setRightShareButton(listener: OnClickListener?) {
        rightImageButton.visibility = View.GONE
        rightActionButton.visibility = View.GONE
        rightShareButton.visibility= View.VISIBLE
        rightShareButton.setOnClickListener(listener)
    }

    fun setRightImageButton(drawableId : Int, listener: OnClickListener?) {
        rightActionButton.visibility = View.GONE
        rightShareButton.visibility= View.GONE

        rightImageButton.visibility = View.VISIBLE

        rightImageButton.setBackgroundDrawable(getDrawable(context, drawableId))

        rightImageButton.setOnClickListener(listener)
    }

    fun hideRightButton() {
        rightImageButton.visibility = View.GONE
        rightActionButton.visibility = View.GONE
        rightShareButton.visibility= View.GONE
    }


    fun setSearchBarListener(listener: View.OnClickListener) {
        searchBoxContainer.setOnClickListener(listener)
    }
}