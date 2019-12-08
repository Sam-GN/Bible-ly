package edu.calbaptist.bible_ly.ui.dialoge

import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import edu.calbaptist.bible_ly.BiblelyClass
import edu.calbaptist.bible_ly.FirestoreRepository
import edu.calbaptist.bible_ly.R

import edu.calbaptist.bible_ly.activity.MainActivity
import kotlinx.android.synthetic.main.dialogue_new_class.view.*

private const val DIALOG_CLASS = "DialogDate"
private const val FINAL_CHOOSE_PHOTO = 122


private lateinit var clss: BiblelyClass
private var classPath:String? = ""
private var isNew:Boolean? = true
private lateinit var dialogView: View


class ClassDialog: DialogFragment(){
    interface Callback {
        fun onClassCreated()
    }

    private var mListener: Callback? = null
    override fun onAttach(activity: Activity?) {


        super.onAttach(activity)
    }

    override fun onDetach() {
        //mListener = null
        super.onDetach()
    }
    private var uri: Uri? =null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
       // val rootView = inflater.inflate(R.layout.fraglayout, container)
        classPath = arguments?.getString("classPath")
        isNew = arguments?.getBoolean("isNew")

        if(!isNew!!){
            FirestoreRepository()
                .getClass(classPath!!){
                clss = it
                updateui()
                mListener = activity as Callback?
            }
        }
        return createDialog()
    }
    private fun updateui(){

        if(!isNew!!){
            dialogView.et_class_new_description.setText( clss!!.description)
            dialogView.et_class_new_teacher.setText( clss!!.teacher!!.firstName+" "+ clss!!.teacher!!.lastName)
            dialogView.et_class_new_title.setText( clss!!.name)
            //dialogView.ib_class_new_classLogo.hesetGlide(clss.classLogo,false)
            if(clss.classLogo!=""){
                dialogView.iv_class_new_deleteLogo.visibility = View.VISIBLE
            }
            Glide.with(this)
                .asBitmap()
                .load(clss.classLogo)
                .into(object : SimpleTarget<Bitmap>(dialogView.ib_class_new_classLogo.width, dialogView.ib_class_new_classLogo.height){
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        dialogView.ib_class_new_classLogo.setImageBitmap(resource)
                    }
                    override fun onLoadCleared(placeholder: Drawable?) {
                        // this is called when imageView is cleared on lifecycle call or for
                        // some other reason.
                        // if you are referencing the bitmap somewhere else too other than this imageView
                        // clear it here as you can no longer have the bitmap
                    }
                })
          /*  Glide.with( dialogView.ib_class_new_classLogo.context)

                .applyDefaultRequestOptions( RequestOptions()
                    .placeholder(R.drawable.ic_class_logo_default2)
                    .error(R.drawable.ic_class_logo_default2)
                )
                //.applyDefaultRequestOptions( RequestOptions())
                //.setDefaultRequestOptions(RequestOptions())
                .load(clss.classLogo)
                .apply(RequestOptions().transforms(CenterCrop()))
                //.apply(RequestOptions.centerCropTransform())
//                         .apply(RequestOptions.circleCropTransform())

                .into(dialogView.ib_class_new_classLogo)
*/
        }
    }
    private fun createDialog():Dialog{
      val  view =LayoutInflater.from(requireContext()).inflate(R.layout.dialogue_new_class, null)
        dialogView = view
   //     val view = layoutInflater.inflate(R.layout.event_detailed_fragment, null)


        view.et_class_new_teacher.let {
            it.setText(MainActivity.user.userName)
            it.isEnabled = false
        }
        view.ib_class_new_classLogo.apply {

            setOnClickListener {

                val checkSelfPermission = ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (checkSelfPermission != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                }
                else{
                    openAlbum()
                }
            }

        }
        view.iv_class_new_deleteLogo.apply {
            setOnClickListener {
                view.ib_class_new_classLogo.setImageResource(R.drawable.ic_menu_camera)
                uri = null
                clss.classLogo = ""
               view.iv_class_new_deleteLogo.visibility = View.GONE
            }
        }
        var dialogeBuilder =AlertDialog.Builder(requireActivity())
            .setCancelable(false)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setNegativeButton(getString(R.string.close), DialogInterface.OnClickListener { dialog, which ->

            })
            .setPositiveButton(if(isNew!!){getString(R.string.create)}else{getString(
                R.string.save
            )}, DialogInterface.OnClickListener { dialog, which ->

                if(isNew!!) {
                    FirestoreRepository().saveClass(
                        this.uri,
                        dialogView.et_class_new_title.text.toString(),
                        dialogView.et_class_new_description.text.toString()
                        )
                    targetFragment?.let { fragment ->
                        (fragment as Callback).onClassCreated()

                    }
                } else {
                    FirestoreRepository().editClass(
                        clss.classID,
                        clss.classLogo,
                        this.uri,
                        dialogView.et_class_new_title.text.toString(),
                        dialogView.et_class_new_description.text.toString()
                    ) {
                        //(activity as ClassSingleActivity).onClassCreated()
                        mListener!!.onClassCreated()
                       //(activity as ClassSingleActivity).reload()


                    }

                }

                //fab.collapse()
            }
            ).setView(view)
        var dialoge = dialogeBuilder.create()
        dialoge.show()
        return dialoge
    }
    private fun openAlbum(){
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, FINAL_CHOOSE_PHOTO)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1 ->
                if (grantResults.isNotEmpty() && grantResults.get(0) ==PackageManager.PERMISSION_GRANTED){
                    openAlbum()
                }
                else {
                    Toast.makeText(requireContext(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            /*FINAL_TAKE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    val bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri))
                    picture!!.setImageBitmap(bitmap)
                }*/
            FINAL_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
//                       After 4.4
                        handleImageOnKitkat(data)
                    }
                    else{
//                       Before 4.4
                        handleImageBeforeKitkat(data)
                    }
                }
        }
    }
    @TargetApi(19)
    private fun handleImageOnKitkat(data: Intent?) {
        var imagePath: String? = null
        val uri = data!!.data
        if (DocumentsContract.isDocumentUri(requireContext(), uri)){
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri.authority){
                val id = docId.split(":")[1]
                val selsetion = MediaStore.Images.Media._ID + "=" + id
                imagePath = imagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selsetion)
            }
            else if ("com.android.providers.downloads.documents" == uri.authority){
                val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(docId))
                imagePath = imagePath(contentUri, null)
            }
        }
        else if ("content".equals(uri.scheme, ignoreCase = true)){
            imagePath = imagePath(uri, null)
        }
        else if ("file".equals(uri.scheme, ignoreCase = true)){
            imagePath = uri.path
        }
        displayImage(imagePath,uri)
    }


    private fun handleImageBeforeKitkat(data: Intent?) {}

    private fun imagePath(uri: Uri?, selection: String?): String {
        var path: String? = null

        val cursor = requireActivity().contentResolver.query(uri, null, selection, null, null )
        if (cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }

    private fun displayImage(imagePath: String?,uri: Uri){
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            dialogView.ib_class_new_classLogo?.setImageBitmap(Bitmap.createScaledBitmap(bitmap,
                dialogView.ib_class_new_classLogo.width,
                dialogView.ib_class_new_classLogo.height,false))
            dialogView.iv_class_new_deleteLogo.visibility = View.VISIBLE
            this.uri = uri

        }
        else {
            Toast.makeText(requireContext(),
                R.string.failed_get_image, Toast.LENGTH_SHORT).show()
        }
    }

   companion object{
       fun newInstance(isNew:Boolean,classPath:String):DialogFragment{
           val frag = ClassDialog()
           val args = Bundle()
           args.putBoolean("isNew", isNew)
           args.putString("classPath",classPath)
           frag.arguments = args
           return frag
       }
   }
}