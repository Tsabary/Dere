package co.getdere.models

class Comments (val authorId : String, val content : String, val timeStamp : Long, val ImageId : String) {
    constructor() : this("","",0, "")
}
