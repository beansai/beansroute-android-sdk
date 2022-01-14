package ai.beans.common.networking

class Envelope<T>(var success : Boolean = false,
                  var data: T ?= null,
                  var meta : Meta ?= null) {
    var error : ApiError ?= null
}