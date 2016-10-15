package com.basecolon.uisless.model

data class CourseModel(
    var open: String? = null,

    var crn: String? = null,
    var subject: String? = null,
    var courseNumber: String? = null,
    var section: String? = null,

    var campus: String? = null,

    var credits: String? = null,
    var title: String? = null,

    var days: String? = null,
    var time: String? = null,

    var remainingSeats: String? = null,
    var waitListActual: String? = null,
    var waitListRemaining: String? = null,

    var instructor: String? = null,
    var date: String? = null
)
