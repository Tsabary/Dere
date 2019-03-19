package co.getdere.Models

class Comments (var authorId : String, var content : String, var timeStamp : Long) {
    constructor() : this("","",0)
}
