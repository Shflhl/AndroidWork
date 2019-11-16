let PhotoModel=require('../model/photo').PhotoModel
function findAllPhotos(callback) {
    PhotoModel.find({},function (err,photos) {
      callback(photos)
    })
}
function findPhotoByImgName(imgName,callback) {
    PhotoModel.find({name:imgName},function (err,photos) {
        callback(photos)
    })
}
function addPhoto(photo,callback){
    PhotoModel.create(photo,function (err,newPhoto) {
        callback(newPhoto)
    })
}

function deletPhoto(id,callback){
    PhotoModel.deleteOne({_id:id},function (err,msg) {
        if(!err) callback({})
    })
}

function updatePhoto(id,photo,callback){
    PhotoModel.findByIdAndUpdate(id,photo,function (err,old) {
        if(!err){
            PhotoModel.findOne({_id:id},function (err,newPhoto) {
                if(!err) callback(newPhoto)
            })
        }
    })
}

exports.findAllPhotos=findAllPhotos
exports.addPhoto=addPhoto
exports.deletPhoto=deletPhoto
exports.updatePhoto=updatePhoto