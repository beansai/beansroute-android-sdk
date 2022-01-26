package ai.beans.common.web

import ai.beans.common.R
import ai.beans.common.ui.core.BeansFragment
import ai.beans.common.widgets.CommonToolbar
import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.appcompat.app.AlertDialog


open class WebviewFragment : BeansFragment() {
    private var callback: ValueCallback<Array<Uri?>?>?= null
    private var url = String()
    private var builder: AlertDialog.Builder? = null
    protected var webView: WebView? = null
    private var title = String()
    protected var currentUrl : String ?= null
    private var showShareButton = false
    private var showToolbar = true
    private var showBackButton = false
    private var webViewSavedState : Bundle?= null

    val FILE_PICKER_REQUEST = 12


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments != null) {
            if(savedInstanceState == null) {
                url = arguments?.getString("URL").toString()
            } else {
                webViewSavedState = savedInstanceState.getBundle("WEB_VIEW_INFO")
            }
            title = arguments?.getString("TITLE").toString()
            showShareButton = arguments?.getBoolean("SHOW_SHARE", false)!!
            showToolbar = arguments?.getBoolean("SHOW_TOOLBAR", true)!!
            showBackButton = arguments?.getBoolean("SHOW_BACK_BUTTON", false)!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_web_view, container, false)
        webView = v.findViewById(R.id.webView)
        setupWebView(webViewSavedState)

        //Alert builder
        builder = AlertDialog.Builder(this.context!!)

        if(!showToolbar) {
            var toolbar = v.findViewById<CommonToolbar>(R.id.commonToolbar)
            toolbar?.visibility = View.GONE
        }

        if(showBackButton) {
            var toolbar = v.findViewById<CommonToolbar>(R.id.commonToolbar)
            toolbar?.setLeftBackButton(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    getMainActivity()?.onBackPressed()
                }
            })
        }

        return v
    }

    override fun onPause() {
        super.onPause()
        if(webView?.url != null) {
            url = webView?.url!!
        }
        webViewSavedState = Bundle()
        webView?.saveState(webViewSavedState!!)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBundle("WEB_VIEW_INFO", webViewSavedState)
    }

    private fun setupWebView(savedInstanceState: Bundle?) {
        this.webView?.setWebViewClient(WebViewBeans())
        this.webView?.setWebChromeClient(BeansChromeClient())
        this.webView?.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY)
        this.webView?.setScrollbarFadingEnabled(false)
        this.webView?.getSettings()?.setJavaScriptEnabled(true)
        this.webView?.getSettings()?.setJavaScriptCanOpenWindowsAutomatically(true)

        if(savedInstanceState != null) {
            webView?.restoreState(savedInstanceState)
        } else {
            webView?.loadUrl(getUrl())
        }
    }

    open fun getUrl() : String {
        return url
    }

    open fun hideToolbar() {
        showToolbar = false
        var toolbar = view?.findViewById<CommonToolbar>(R.id.commonToolbar)
        toolbar?.visibility = View.GONE
    }

    open fun showToolbar() {
        showToolbar = true
        var toolbar = view?.findViewById<CommonToolbar>(R.id.commonToolbar)
        toolbar?.visibility = View.VISIBLE
    }

    open fun loadUrl(url : String) {
        this.url = url
        this.webView?.loadUrl(getUrl())
    }

    internal inner class WebViewBeans : WebViewClient() {
        var progressDialog: ProgressDialog? = null

        override fun onPageFinished(view: WebView, url: String) {
            try {
                Log.d("WebView", "onPageFinished" + " " + url)
                this@WebviewFragment.url = url
                if (this.progressDialog!!.isShowing) {
                    this.progressDialog!!.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        override fun onLoadResource(view: WebView, url: String) {
            super.onLoadResource(view, url)
            Log.d("WebView", "onLoadResource" + " " + url)
            if (this.progressDialog == null) {
                this.progressDialog = ProgressDialog(activity)
                this.progressDialog!!.setMessage("Loading...")
                this.progressDialog!!.show()
            }
        }

        override fun onReceivedSslError(view: WebView, sslErrorHandler: SslErrorHandler, error: SslError) {
            var message = "SSL Certificate error."
            when (error.primaryError) {
                SslError.SSL_UNTRUSTED -> message = "The certificate authority is not trusted."
                SslError.SSL_EXPIRED -> message = "The certificate has expired."
                SslError.SSL_IDMISMATCH -> message = "The certificate hostname mismatch."
                SslError.SSL_NOTYETVALID -> message = "The certificate is not yet valid."
            }
            message += " Do you want to continue anyway?"

            builder?.setTitle("SSL Certificate Error")
            builder?.setMessage(message)
            builder?.setPositiveButton("continue", DialogInterface.OnClickListener { dialog, which -> sslErrorHandler.proceed() })
            builder?.setNegativeButton("cancel", DialogInterface.OnClickListener { dialog, which -> sslErrorHandler.cancel() })
            val dialog = builder?.create()
            dialog?.show()
        }

    }

    inner class BeansChromeClient : WebChromeClient() {
        override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri?>?>?,
                                       fileChooserParams: FileChooserParams?): Boolean {

            callback = filePathCallback

            val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
            contentSelectionIntent.type = "image/*"

            val chooserIntent = Intent(Intent.ACTION_CHOOSER)
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")

            startActivityForResult(chooserIntent, FILE_PICKER_REQUEST)

            return true
        }
    }

    override fun setScreenName() {
        screenName = "webView"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode != FILE_PICKER_REQUEST || callback == null) {
            super.onActivityResult(requestCode, resultCode, data)
        } else {

            var results: Array<Uri?>? = null

            // Check that the response is a good one

            // Check that the response is a good one
            if (resultCode === Activity.RESULT_OK && data != null) {
                results = arrayOf(data.data!!)
                callback?.onReceiveValue(results)
            }else {
                callback?.onReceiveValue(null)
            }
        }
    }

    override fun handleBack(): Boolean {
        if(webView!!.canGoBack()) {
            webView!!.goBack()
            return true
        } else {
            return false
        }
    }

}