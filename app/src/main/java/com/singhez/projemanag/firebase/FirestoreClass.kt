package com.singhez.projemanag.firebase

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.singhez.projemanag.activities.*
import com.singhez.projemanag.models.Board
import com.singhez.projemanag.models.Card
import com.singhez.projemanag.models.User
import com.singhez.projemanag.utils.Constants

class FirestoreClass {

    private  val firestore = FirebaseFirestore.getInstance()

    fun updateBoardDetails(activity: MembersActivity, hashMap: HashMap<String, Any>,
                           boardID : String){
        firestore.collection(Constants.BOARDS)
            .document(boardID)
            .update(hashMap)
            .addOnSuccessListener {
                activity.memberUpdatedSuccessfully()
            }
            .addOnFailureListener {  e ->
                Log.e(e.message,e.message!!)
                activity.hideProgressDialog()
            }
    }

    fun getMemberDetails(activity: MembersActivity, email: String){
        firestore.collection(Constants.USER)
            .whereEqualTo(Constants.email, email)
            .get()
            .addOnSuccessListener { document ->
                activity.hideProgressDialog()
                if (document.size() > 0){
                    activity.memberFound(document.documents[0].toObject(User::class.java)!!)
                }else{
                    Toast.makeText(activity,
                        "Oops... No member found with this email",
                        Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(e.message,e.message!!)
                activity.hideProgressDialog()
            }
    }

    fun getMembersListDetails(activity: Activity, idList : ArrayList<String>){
        firestore.collection(Constants.USER)
            .whereIn(Constants.id,idList)
            .get()
            .addOnSuccessListener {
                    documents ->
                val usersList = ArrayList<User>()
                for (document in documents){
                    val user = document.toObject(User::class.java)
                    usersList.add(user)
                }
                if (activity is MembersActivity){
                    activity.showMembers(usersList)
                }else if (activity is CardDetailsActivity){
                    activity.getAllMemberList(usersList)
                }
            }
            .addOnFailureListener {e ->
                Log.e(e.message,e.message!!)
                if (activity is MembersActivity){
                    activity.hideProgressDialog()
                }else if(activity is CardDetailsActivity){
                    activity.hideProgressDialog()
                }
            }

    }

    fun deleteBoard(activity: TaskListActivity, boardID: String){
        firestore.collection(Constants.BOARDS)
                .document(boardID)
                .delete()
                .addOnSuccessListener {
                    activity.deleteBoardSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e(e.message,e.message!!)
                    activity.hideProgressDialog()
                }
    }

    fun addUpdateTaskList(activity: Activity, board: Board){

        val hasMapTaskList = HashMap<String, Any>()
        hasMapTaskList[Constants.TASK_LIST] = board.task_list
        firestore.collection(Constants.BOARDS)
                .document(board.document_ID)
                .update(hasMapTaskList)
                .addOnSuccessListener {
                    if (activity is TaskListActivity){
                        activity.addUpdateTaskListSuccess()
                    }else if(activity is CardDetailsActivity){
                        activity.updateCardSuccess()
                    }
                }
                .addOnFailureListener {
                    e->
                    Log.e(e.message,e.message!!)
                    if (activity is TaskListActivity){
                        activity.hideProgressDialog()
                    }else if(activity is CardDetailsActivity){
                        activity.hideProgressDialog()
                    }
                }

    }

    fun registerUser(activity: SignUpActivity, userInfo: User){
        firestore.collection(Constants.USER)
                .document(getCurrentUserID())
                .set(userInfo, SetOptions.merge())
                .addOnSuccessListener {
                    activity.userRegisteredSuccessfully()
                }
                .addOnFailureListener { e->
                    Log.e(e.message,e.message!!)
                    activity.hideProgressDialog()
                }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board){
        firestore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                activity.boardCreatedSuccessfully()
            }
            .addOnFailureListener {
                e ->
                Log.e(e.message,e.message!!)
                activity.hideProgressDialog()
            }
    }

    fun loadUserDetails(activity: Activity){
        firestore.collection(Constants.USER)
                .document(getCurrentUserID())
                .get()
                .addOnSuccessListener { document ->
                    val user = document.toObject(User::class.java)
                    when(activity){
                        is MainActivity -> {
                            activity.updateNavigationUserDetails(user!!)
                        }
                    }
                }
                .addOnFailureListener {
                    e->
                    Log.e(e.message,e.message!!)
                    when(activity){
                        is MainActivity -> {
                            activity.hideProgressDialog()
                        }
                    }
                }
    }

    fun getBoardDetails(activity: TaskListActivity,boardID : String){
        firestore.collection(Constants.BOARDS)
                .document(boardID)
                .get()
                .addOnSuccessListener { document ->
                    activity.showTasksList(document.toObject(Board::class.java)!!)
                }
                .addOnFailureListener {
                    e->
                    Log.e(e.message,e.message!!)
                    activity.hideProgressDialog()
                }
    }

    fun updateUserDetails(activity: MyProfileActivity, userHashMap: HashMap<String,Any>){
        firestore.collection(Constants.USER)
            .document(getCurrentUserID())
            .update(userHashMap)
            .addOnSuccessListener {
                activity.detailsUpdatedSuccessfully()
            }
            .addOnFailureListener {
                    e->
                Log.e(e.message,e.message!!)
                activity.hideProgressDialog()
            }
    }

    fun getBoardsList(activity: MainActivity){
        firestore.collection(Constants.BOARDS)
            .whereArrayContains(Constants.ASSIGNED_TO,getCurrentUserID())
            .get()
            .addOnSuccessListener { documents ->
                val boardList = ArrayList<Board>()
                for (document in documents) {
                    val board : Board = document.toObject(Board::class.java)
                    board.document_ID = document.id
                    boardList.add(board)
                }
                activity.showBoards(boardList)
            }
            .addOnFailureListener {
                    e->
                Log.e(e.message,e.message!!)
                activity.hideProgressDialog()
            }

    }

    fun getCurrentUserID() : String{

        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserID = ""
        if (currentUser != null){
            currentUserID = currentUser.uid
        }
        return currentUserID

        //return FirebaseAuth.getInstance().currentUser!!.uid
    }


}