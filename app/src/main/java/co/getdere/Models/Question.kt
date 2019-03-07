package co.getdere.Models

import java.util.*

class Question(val title : String, val details : String, val tags : String, val timestamp: String) {
    constructor() : this("","","", "")
}
