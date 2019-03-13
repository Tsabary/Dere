package co.getdere.Models

class Images (val id : String, val image : String, val photographer : String, val link : String, val details : String, val location : MutableList<Double>, val timestamp : Long){
    constructor() : this("","","","","", mutableListOf(),0)
}