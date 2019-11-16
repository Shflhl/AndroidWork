var express = require('express');
var router = express.Router();
var userController=require('../controller/UserController')
let PhotoModel=require('../model/photo').PhotoModel


// exports.findAllPhotos=findAllPhotos
// exports.addPhoto=addPhoto
// exports.deletPhoto=deletPhoto
// exports.updatePhoto=updatePhoto


const fs=require('fs')
const http=require('http')
//文件上传中间件(指定上传的临时文件夹是/uploads)
const multer=require('multer')
let upload = multer({ dest: 'uploads/' })
const FILE_PATH="public/images/"
const access_token="24.6586f00b56156327a80484766e287a0e.2592000.1575704638.282335-17718208"
const face_detect_url = "https://aip.baidubce.com/rest/2.0/face/v3/detect?access_token=24.6586f00b56156327a80484766e287a0e.2592000.1575704638.282335-17718208"
/*
Header：
    参数	         值
    Content-Type	application/json
Body中放置请求参数，参数详情如下：
    image		    string	图片信息(不包括 data:image/jpg;base64,)
    image_type		string	图片类型 "BASE64"
 */

function base64_encode(file) {
    // read binary data
    var bitmap = fs.readFileSync(file);
    // convert binary data to base64 encoded string
    return new Buffer(bitmap).toString('base64');
}
// request({
//     url: face_detect_url,
//     method: "post",//如果是post就涉及到跨域的问题了
//     json: true,
//     headers: {
//         "content-type": "application/json",
//     },
//     body: {
//         image:base64_encode(req.file.path),
//         image_type:'BASE64'
//     }
// }, function (error, response, body) {
//     if (!error && response.statusCode == 200) {
//         console.log(body);
//     }
// });



router.post('/upload', upload.single('avatar'), function (req, res, next) {
    var msg;
    fs.exists(FILE_PATH+req.file.originalname, function(exists) {
        if(!exists){
            //（1）将临时文件上传到/public/images中
            let output=fs.createWriteStream(FILE_PATH+req.file.originalname)
            let input=fs.createReadStream(req.file.path)
            input.pipe(output)
            // （2）把上传的图片转为BASE64存到数据库中
            var con = base64_encode(req.file.path)
            var photo = new PhotoModel({ name : req.file.originalname,
                size: req.file.size,
                content: con,
                createDate : new Date()
            });
            userController.addPhoto(photo,function (newPhoto) {
                console.log("添加到数据库成功")
            })
            msg = "文件上传成功"
        }else {
            msg = "文件上传失败(文件已存在)"
            console.log("文件已存在");
        }
        // 再把信息传回手机
        res.json(msg)
    });
})


//接收前端的请求，返回上传图片的列表
router.get("/photos",function (req,res) {
    userController.findAllPhotos(function (photos) {
        res.json(photos)
    })

})
//接收前端的请求，返回上传图片的列表
router.get("/files",function (req,res) {
    fs.readdir('public/images',function (err,dir) {
        console.log(dir)
        res.json(dir)
    })
})
//返回所有上传图片名称
router.get("/all_photos",function (req,res) {
    var all_photos = fs.readdirSync(FILE_PATH);
    res.json(all_photos)
})

module.exports = router;
