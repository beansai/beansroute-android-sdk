package ai.beans.common.widgets.stops

import ai.beans.common.pojo.RouteStopStatus
import ai.beans.common.pojo.RouteStopType
import ai.beans.common.utils.MapMarkerBitmapCache
import ai.beans.common.widgets.markers.IconAttributes
import ai.beans.common.R
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.maps.android.ui.IconGenerator

class BeansStopMarkerIconImpl : RelativeLayout {

    companion object {
    }
    var smallIconRouteNumber: TextView? = null
    var smallIconTitle: TextView? = null
    var largeIconRouteNumber: TextView? = null
    var largeIconTitle: TextView? = null


    var smallIconContainer : ConstraintLayout ?= null
    var smallIconImageContainer : ConstraintLayout ?= null


    var largeIconContainer : ConstraintLayout ?= null
    var largeIconImageContainer : ConstraintLayout ?= null


    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
    }

    init {
        val v = LayoutInflater.from(context).inflate(R.layout.beans_route_stop_marker, this, true) as RelativeLayout
        smallIconContainer = v.findViewById(R.id.small_icon_container)
        smallIconImageContainer = v.findViewById(R.id.small_icon)

        largeIconContainer = v.findViewById(R.id.large_icon_container)
        largeIconImageContainer = v.findViewById(R.id.large_icon)

        smallIconRouteNumber = v.findViewById(R.id.small_icon_number)
        largeIconRouteNumber = v.findViewById(R.id.large_icon_number)

        smallIconTitle = v.findViewById(R.id.small_icon_text)
        largeIconTitle = v.findViewById(R.id.large_icon_text)
    }

    fun setupIcon(iconGenerator: IconGenerator, attributes : IconAttributes) : Bitmap {

        var bitmapKey: String? = null
        bitmapKey = generateKey(
            attributes.type, attributes.status, attributes.showMarkerWithTextLabel,
            attributes.hasApartments, attributes.showSelected, attributes.number,
            attributes.addressString
        )

        var bitmap = MapMarkerBitmapCache.bitmapHashMap[bitmapKey]
        if (bitmap != null) {
            return bitmap
        } else {

            if (attributes.status == RouteStopStatus.NEW && attributes.number != null) {
                //Render a dark icon to show its in the route
                when (attributes.type) {
                    RouteStopType.DROPOFF -> {
                        setDropOffIconDark(
                            attributes.status, attributes.hasApartments,
                            attributes.showMarkerWithTextLabel, attributes.showSelected,
                            attributes.number, attributes.addressString
                        )
                    }

                    RouteStopType.PICKUP -> {
                        setPickupIconDark(
                            attributes.status, attributes.hasApartments,
                            attributes.showMarkerWithTextLabel, attributes.showSelected,
                            attributes.number, attributes.addressString
                        )
                    }
                }
            } else {
                when (attributes.type) {
                    RouteStopType.DROPOFF -> {
                        setDropOffIcon(
                            attributes.status, attributes.hasApartments,
                            attributes.showMarkerWithTextLabel, attributes.showSelected,
                            attributes.number, attributes.addressString
                        )
                    }

                    RouteStopType.PICKUP -> {
                        setPickupIcon(
                            attributes.status, attributes.hasApartments,
                            attributes.showMarkerWithTextLabel, attributes.showSelected,
                            attributes.number, attributes.addressString
                        )
                    }
                }
            }

            iconGenerator.setContentView(this)
            bitmap = iconGenerator.makeIcon()
            MapMarkerBitmapCache.bitmapHashMap[bitmapKey] = bitmap
            return bitmap
        }
    }

    fun setupIcon(iconGenerator: IconGenerator, type : RouteStopType?, status : RouteStopStatus?,
                  showMarkerWithText : Boolean = false,
                  hasApartments : Boolean = false,
                  showSelected : Boolean = false,
                  markerNumber : Int?,
                  markerString : String? = null ) : Bitmap {

        var bitmapKey = generateKey(type, status, showMarkerWithText, hasApartments, showSelected, markerNumber, markerString)

        var bitmap = MapMarkerBitmapCache.bitmapHashMap[bitmapKey]
        if(bitmap !=null) {
            return bitmap
        } else {

            if (status == RouteStopStatus.NEW && markerNumber != null) {
                //Render a dark icon to show its in the route
                when (type) {
                    RouteStopType.DROPOFF -> {
                        setDropOffIconDark(status, hasApartments, showMarkerWithText, showSelected, markerNumber, markerString)
                    }

                    RouteStopType.PICKUP -> {
                        setPickupIconDark(status, hasApartments, showMarkerWithText, showSelected, markerNumber, markerString)
                    }
                }
            } else {
                when (type) {
                    RouteStopType.DROPOFF -> {
                        setDropOffIcon(status, hasApartments, showMarkerWithText, showSelected, markerNumber, markerString)
                    }

                    RouteStopType.PICKUP -> {
                        setPickupIcon(status, hasApartments, showMarkerWithText, showSelected, markerNumber, markerString)
                    }
                }
            }

            iconGenerator.setContentView(this)
            bitmap = iconGenerator.makeIcon()
            MapMarkerBitmapCache.bitmapHashMap[bitmapKey] = bitmap
            return bitmap
        }
    }

    private fun generateKey(type : RouteStopType?, status : RouteStopStatus?,
                            showMarkerWithText : Boolean = false,
                            hasApartments : Boolean = false,
                            showSelected : Boolean = false,
                            num : Int? = null,
                            titleStr : String ? = null): String {

        var stringKey = type.toString()
        stringKey += status.toString()
        if(hasApartments) {
            stringKey += "hasApt"
        }
        if(showSelected) {
            stringKey += "Selected"
        }
        if(num != null) {
            stringKey += num.toString()
        }

        if(showMarkerWithText) {
            if (titleStr != null) {
                stringKey += titleStr
            }
        }

        return stringKey
    }

    private fun generateKeyForLassoedMarker(attributes: IconAttributes): String {
        var stringKey = attributes.type.toString()
        stringKey += attributes.status.toString()
        if(attributes.hasApartments) {
            stringKey += "hasApt"
        }
        if(attributes.showMarkerWithTextLabel) {
            stringKey += attributes.addressString
        }
        stringKey += "Lassoed"
        return stringKey

    }

    private fun generateKeyForLastItem(attributes: IconAttributes): String {
        var stringKey = attributes.type.toString()
        stringKey += attributes.status.toString()
        if(attributes.hasApartments) {
            stringKey += "hasApt"
        }
        if(attributes.showMarkerWithTextLabel) {
            stringKey += attributes.addressString
        }
        stringKey += "LastItem"
        return stringKey

    }

    private fun generateKeyForOptimizedMarker(type : RouteStopType?, status : RouteStopStatus?,
                            hasApartments : Boolean = false,
                            showSelected : Boolean = false,
                            num : Int? = null): Int {
        return 0
    }


    fun setUpLastStopIcon(iconGenerator: IconGenerator, iconAttributes: IconAttributes) : Bitmap {

        var container :ConstraintLayout ?= null

        var bitmapKey = generateKeyForLastItem(iconAttributes)

        var bitmap = MapMarkerBitmapCache.bitmapHashMap[bitmapKey]
        if(bitmap !=null) {
            return bitmap
        } else {
            smallIconContainer?.visibility = View.GONE
            largeIconContainer?.visibility = View.VISIBLE
            container = largeIconImageContainer
            if (iconAttributes.hasApartments) {
                container?.setBackgroundResource(R.drawable.pin_apt_opt_last_stop)
            } else {
                when (iconAttributes.type) {
                    RouteStopType.DROPOFF -> {
                        container?.setBackgroundResource(R.drawable.pin_stop_lassoed_last_big)
                    }

                    RouteStopType.PICKUP -> {
                        container?.setBackgroundResource(R.drawable.pin_pickup_lassoed_last_stop_large)
                    }
                }
            }
            if(iconAttributes.showMarkerWithTextLabel) {
                if(iconAttributes.addressString != null) {
                    largeIconTitle?.visibility = View.VISIBLE
                    largeIconTitle?.text =  iconAttributes.addressString
                } else {
                    largeIconTitle?.visibility = View.GONE
                    largeIconTitle?.text =  ""
                }
            } else {
                largeIconTitle?.text =  ""
            }


            iconGenerator.setContentView(this)
            bitmap = iconGenerator.makeIcon()
            MapMarkerBitmapCache.bitmapHashMap[bitmapKey] = bitmap
            return bitmap
        }
    }

    fun setupIconForLasso(iconGenerator: IconGenerator, attributes : IconAttributes) : Bitmap {

        var container :ConstraintLayout ?= null
        var bitmapKey = generateKeyForLassoedMarker(attributes)

        var bitmap = MapMarkerBitmapCache.bitmapHashMap[bitmapKey]
        if(bitmap !=null) {
            return bitmap
        } else {

            smallIconContainer?.visibility = View.VISIBLE
            largeIconContainer?.visibility = View.GONE
            container = smallIconImageContainer

            if (attributes.hasApartments) {
                container?.setBackgroundResource(R.drawable.pin_apt_opt_small)
            } else {
                when (attributes.type) {
                    RouteStopType.DROPOFF -> {
                        container?.setBackgroundResource(R.drawable.pin_stop_lassoed_small)
                    }

                    RouteStopType.PICKUP -> {
                        container?.setBackgroundResource(R.drawable.pin_pickup_lassoed_small)
                    }
                }
            }
            if(attributes.showMarkerWithTextLabel) {
                if(attributes.addressString != null) {
                    smallIconTitle?.visibility = View.VISIBLE
                    smallIconTitle?.text =  attributes.addressString
                } else {
                    smallIconTitle?.visibility = View.GONE
                    smallIconTitle?.text =  ""
                }
            } else {
                smallIconTitle?.visibility = View.GONE
                smallIconTitle?.text =  ""
            }
            iconGenerator.setContentView(this)
            bitmap = iconGenerator.makeIcon()
            MapMarkerBitmapCache.bitmapHashMap[bitmapKey] = bitmap
            return bitmap
        }
    }


//    fun setupIconForLasso(
//        iconGenerator: IconGenerator,
//        type: RouteStopType?,
//        status: RouteStopStatus?,
//        hasApartments: Boolean,
//        showMarkerWithText: Boolean = false,
//        addressString: String? = null
//    ) : Bitmap {
//
//        var container :RelativeLayout ?= null
//        var bitmapKey = generateKeyForLassoedMarker(type, status, hasApartments, showMarkerWithText )
//
//        var bitmap = MapMarkerBitmapCache.bitmapHashMap[bitmapKey]
//        if(bitmap !=null) {
//            return bitmap
//        } else {
//
//            smallIconContainer?.visibility = View.VISIBLE
//            largeIconContainer?.visibility = View.GONE
//            container = smallIconImageContainer
//
//            if (hasApartments) {
//                container?.setBackgroundResource(R.drawable.pin_apt_lassoed_small)
//            } else {
//                when (type) {
//                    RouteStopType.DROPOFF -> {
//                        container?.setBackgroundResource(R.drawable.pin_stop_lassoed_small)
//                    }
//
//                    RouteStopType.PICKUP -> {
//                        container?.setBackgroundResource(R.drawable.pin_pickup_lassoed_small)
//                    }
//                }
//            }
//            iconGenerator.setContentView(this)
//            bitmap = iconGenerator.makeIcon()
//            MapMarkerBitmapCache.bitmapHashMap[bitmapKey] = bitmap
//            return bitmap
//        }
//    }


    private fun setDropOffIconDark(status : RouteStopStatus?, hasApartments : Boolean,
                                   showMarkerWithText : Boolean = false,
                                   showSelected : Boolean,
                                   num : Int?,
                                   title : String?) {
        var container :ConstraintLayout ?= null
        var markerNumberTextView : TextView ?= null
        var markerTitleTextView : TextView ?= null

        if(showSelected) {
            largeIconContainer?.visibility = View.VISIBLE
            smallIconContainer?.visibility = View.GONE
            container = largeIconImageContainer
            markerNumberTextView = largeIconRouteNumber
            markerTitleTextView = largeIconTitle
        } else {
            smallIconContainer?.visibility = View.VISIBLE
            largeIconContainer?.visibility = View.GONE
            container = smallIconImageContainer
            markerNumberTextView = smallIconRouteNumber
            markerTitleTextView = smallIconTitle
        }

        if(num != null) {
            markerNumberTextView?.text = num.toString()
            //The text color has to change for apt stops
            if(hasApartments) {
                markerNumberTextView?.setTextColor(resources.getColor(R.color.colorWhite)) //XX marker_number_optimized_color))
            } else {
                markerNumberTextView?.setTextColor(resources.getColor(R.color.colorWhite))
            }
        }

        if(showMarkerWithText) {
            if (title != null) {
                markerTitleTextView?.visibility = View.VISIBLE
                markerTitleTextView?.text = title
            } else {
                markerTitleTextView?.visibility = View.GONE
                markerTitleTextView?.text = ""
            }
        } else {
            markerTitleTextView?.visibility = View.GONE
            markerTitleTextView?.text = ""
        }

        when(status) {
            RouteStopStatus.NEW, RouteStopStatus.IN_PROCESS -> {
                if(hasApartments) {
                    if (showSelected) {
                        container?.setBackgroundResource(R.drawable.pin_apt_opt_big)
                    } else {
                        container?.setBackgroundResource(R.drawable.pin_apt_opt_small)
                    }
                } else {
                    if (showSelected) {
                        container?.setBackgroundResource(R.drawable.pin_stop_opt_large)
                    } else {
                        container?.setBackgroundResource(R.drawable.pin_stop_lassoed_small)
                    }
                }
            }
            else -> {
                container?.setBackgroundResource(R.drawable.route_stop_pin_small)
            }
        }

    }


    private fun setPickupIconDark(status : RouteStopStatus?, hasApartments : Boolean,
                                  showMarkerWithText : Boolean = false,
                                  showSelected : Boolean,
                                  num : Int?,
                                  title : String?) {
        var container :ConstraintLayout ?= null
        var markerNumberTextView : TextView ?= null
        var markerTitleTextView : TextView ?= null
        if(showSelected) {
            largeIconContainer?.visibility = View.VISIBLE
            smallIconContainer?.visibility = View.GONE
            container = largeIconImageContainer
            markerNumberTextView = largeIconRouteNumber
            markerTitleTextView = largeIconTitle
        } else {
            smallIconContainer?.visibility = View.VISIBLE
            largeIconContainer?.visibility = View.GONE
            container = smallIconImageContainer
            markerNumberTextView = smallIconRouteNumber
            markerTitleTextView = smallIconTitle
        }

        if(num != null) {
            markerNumberTextView?.text = num.toString()
            //The text color has to change for apt stops
            if(hasApartments) {
                markerNumberTextView?.setTextColor(resources.getColor(R.color.camera_button_grey))
            } else {
                markerNumberTextView?.setTextColor(resources.getColor(R.color.colorWhite))
            }
        }

        if(showMarkerWithText) {
            if (title != null) {
                markerTitleTextView?.visibility = View.VISIBLE
                markerTitleTextView?.text = title
            } else {
                markerTitleTextView?.visibility = View.GONE
                markerTitleTextView?.text = ""
            }
        } else {
            markerTitleTextView?.visibility = View.GONE
            markerTitleTextView?.text = ""
        }

        when(status) {
            RouteStopStatus.NEW, RouteStopStatus.IN_PROCESS -> {
                if(hasApartments) {
                    if (showSelected) {
                        container?.setBackgroundResource(R.drawable.pin_pickup_opt_large)
                    } else {
                        container?.setBackgroundResource(R.drawable.pin_pickup_lassoed_small)
                    }
                } else {
                    if (showSelected) {
                        container?.setBackgroundResource(R.drawable.pin_pickup_opt_large)
                    } else {
                        container?.setBackgroundResource(R.drawable.pin_pickup_lassoed_small)
                    }
                }
            }
            else -> {
                container?.setBackgroundResource(R.drawable.route_stop_pin_small)
            }
        }
    }

    private fun setDropOffIcon(status : RouteStopStatus?, hasApartments : Boolean,
                               showMarkerWithText : Boolean = false,
                               showSelected : Boolean,
                               num : Int?,
                               title : String?) {
        var container :ConstraintLayout ?= null
        var markerNumberTextView : TextView ?= null
        var markerTitleTextView : TextView ?= null
        if(showSelected) {
            largeIconContainer?.visibility = View.VISIBLE
            smallIconContainer?.visibility = View.GONE
            container = largeIconImageContainer
            markerNumberTextView = largeIconRouteNumber
            markerTitleTextView = largeIconTitle
        } else {
            smallIconContainer?.visibility = View.VISIBLE
            largeIconContainer?.visibility = View.GONE
            container = smallIconImageContainer
            markerNumberTextView = smallIconRouteNumber
            markerTitleTextView = smallIconTitle
        }

        if(num != null) {
            markerNumberTextView?.text = num.toString()
        }

        if(showMarkerWithText) {
            //container?.visibility = GONE
            if (title != null) {
                markerTitleTextView?.visibility = View.VISIBLE
                markerTitleTextView?.text = title
            } else {
                markerTitleTextView?.visibility = View.GONE
                markerTitleTextView?.text = ""
            }
        } else {
            markerTitleTextView?.visibility = View.GONE
            markerTitleTextView?.text = ""
        }

        when(status) {
            RouteStopStatus.NEW, RouteStopStatus.IN_PROCESS -> {
                if(hasApartments) {
                    if (showSelected) {
                        container?.setBackgroundResource(R.drawable.pin_apt_new_big)
                    } else {
                        container?.setBackgroundResource(R.drawable.pin_apt_new)
                    }
                } else {
                    if (showSelected) {
                        container?.setBackgroundResource(R.drawable.pin_stop_new_big)
                    } else {
                        container?.setBackgroundResource(R.drawable.pin_stop_new)
                    }
                }
            }
            RouteStopStatus.FINISHED -> {
                if(hasApartments) {
                    if (showSelected) {
                        container?.setBackgroundResource(R.drawable.pin_apt_done_big)
                    } else {
                        container?.setBackgroundResource(R.drawable.pin_apt_done)
                    }
                } else {
                    if (showSelected) {
                        container?.setBackgroundResource(R.drawable.pin_stop_delivered_big)
                    } else {
                        container?.setBackgroundResource(R.drawable.pin_stop_done)
                    }
                }
            }

            RouteStopStatus.FAILED -> {
                if(hasApartments) {
                    if (showSelected) {
                        container?.setBackgroundResource(R.drawable.pin_apt_attempted_big)
                    } else {
                        container?.setBackgroundResource(R.drawable.pin_apt_attempted)
                    }
                } else {
                    if (showSelected) {
                        container?.setBackgroundResource(R.drawable.pin_stop_attempted_big)
                    } else {
                        container?.setBackgroundResource(R.drawable.pin_stop_attempted)
                    }
                }
            }

            RouteStopStatus.MISLOAD -> {
                if(hasApartments) {
                    if (showSelected) {
                        container?.setBackgroundResource(R.drawable.pin_apt_misload)
                    } else {
                        container?.setBackgroundResource(R.drawable.pin_apt_misload)
                    }
                } else {
                    if (showSelected) {
                        container?.setBackgroundResource(R.drawable.pin_stop_misload)
                    } else {
                        container?.setBackgroundResource(R.drawable.pin_stop_misload)
                    }
                }
            }

            else -> {
                container?.setBackgroundResource(R.drawable.route_stop_pin_small)
            }
        }

    }

    private fun setPickupIcon(status : RouteStopStatus?, hasApartments : Boolean,
                              showMarkerWithText : Boolean = false,
                              showSelected : Boolean,
                              num : Int?,
                              title : String?) {
        var container :ConstraintLayout ?= null
        var markerNumberTextView : TextView ?= null
        var markerTitleTextView : TextView ?= null
        if(showSelected) {
            largeIconContainer?.visibility = View.VISIBLE
            smallIconContainer?.visibility = View.GONE
            container = largeIconImageContainer
            markerNumberTextView = largeIconRouteNumber
            markerTitleTextView = largeIconTitle
        } else {
            smallIconContainer?.visibility = View.VISIBLE
            largeIconContainer?.visibility = View.GONE
            container = smallIconImageContainer
            markerNumberTextView = smallIconRouteNumber
            markerTitleTextView = smallIconTitle
        }

        if(num != null) {
            markerNumberTextView?.text = num.toString()
        }

        if(showMarkerWithText) {
            if (title != null) {
                markerTitleTextView?.visibility = View.VISIBLE
                markerTitleTextView?.text = title
            } else {
                markerTitleTextView?.visibility = View.GONE
                markerTitleTextView?.text = ""
            }
        } else {
            markerTitleTextView?.visibility = View.GONE
            markerTitleTextView?.text = ""
        }

        when(status) {
            RouteStopStatus.NEW, RouteStopStatus.IN_PROCESS -> {
                if (showSelected) {
                    container?.setBackgroundResource(R.drawable.pin_pickup_new_big)
                } else {
                    container?.setBackgroundResource(R.drawable.pin_pickup_new)
                }
            }
            RouteStopStatus.FINISHED -> {
                if (showSelected) {
                    container?.setBackgroundResource(R.drawable.pin_pickup_done_big)
                } else {
                    container?.setBackgroundResource(R.drawable.pin_pickup_done)
                }
            }

            RouteStopStatus.FAILED -> {
                if (showSelected) {
                    container?.setBackgroundResource(R.drawable.pin_pickup_attempted_big)
                } else {
                    container?.setBackgroundResource(R.drawable.pin_pickup_attempted)
                }
            }

            RouteStopStatus.MISLOAD -> {
                if (showSelected) {
                    container?.setBackgroundResource(R.drawable.pin_pickup_misload)
                } else {
                    container?.setBackgroundResource(R.drawable.pin_pickup_attempted)
                }
            }

            else -> {
                container?.setBackgroundResource(R.drawable.route_stop_pin_small)
            }
        }

    }


}