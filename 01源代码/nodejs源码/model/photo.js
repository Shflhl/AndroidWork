const mongoose=require('mongoose')

let PhotoSchema={
    name:String,
    size:Number,
    content:String,
    createDate:Date
}
let PhotoModel=mongoose.model("Photo",PhotoSchema)
exports.PhotoModel=PhotoModel