package edu.calbaptist.bible_ly.ui.board

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import edu.calbaptist.bible_ly.*
import edu.calbaptist.bible_ly.adapter.BoardAdapter
import edu.calbaptist.bible_ly.adapter.EventMutableListAdapter


class BoardFragment : Fragment(),EventMutableListAdapter.OnEventItemSelectedListener ,BoardAdapter.OnBoardItemSelectedListener{
    override fun onEventItemSelected(classItem: Event) {
        var d = EventDialog.newInstance(false, false , classItem.clss!!.classID,classItem.eventID)
//        d.show(requireFragmentManager(),"")

        val fm = requireActivity().supportFragmentManager
        d.show(fm,"EventDialog")
    }

    companion object {

        private const val TAG = "MainActivity"

        private const val RC_SIGN_IN = 9001

        private const val LIMIT = 50
    }
    //    companion object {
//
//        @JvmStatic
//        fun newInstance(eventID: Int) = EventFragment().apply {
//            arguments = Bundle().apply {
//                putInt("REPLACE WITH A STRING CONSTANT", eventID)
//            }
//        }
//    }
//    interface Callbacks {
//        fun onEventSelected(eventID: Int)
//    }
//
//    private var callbacks: Callbacks? = null
    lateinit var firestore: FirebaseFirestore
    lateinit var query: Query
    private lateinit var homeViewModel: HomeViewModel

    private lateinit var notificationRecyclerView: RecyclerView
   // private  var studentAdapter: NotificationAdapter = NotificationAdapter()
   lateinit var adapter: BoardAdapter
    lateinit var adapter2: EventMutableListAdapter
    var eventList = mutableListOf<Event>()
    var doneList = mutableListOf<Boolean>()

    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_board, container, false)

        notificationRecyclerView = root.findViewById(R.id.home_frag_main_recycleView)

        homeViewModel.text.observe(this, Observer {
           /* textView.text = it*/
        })
        linearLayoutManager = LinearLayoutManager(context)
        notificationRecyclerView.layoutManager = linearLayoutManager
      //  updateUI(homeViewModel.events)
        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true)

        // Firestore
        firestore = FirebaseFirestore.getInstance()

        // Get ${LIMIT} restaurants
        query = firestore.collectionGroup("events")
            .orderBy("createdDate", Query.Direction.DESCENDING)
//            .limit(LIMIT.toLong())

        // RecyclerView
        adapter = object : BoardAdapter(query, this@BoardFragment) {
            override fun onDataChanged() {
                // Show/hide content if the query returns empty.
                Log.i("BoardFragment",itemCount.toString())
                eventList =  mutableListOf<Event>()
                if (itemCount == 0) {
                   reloadEvents()
                } else {
                    for(i in 0 until itemCount)
                    {
                        doneList.add(false)
                        val event = getSnapshot(i).toObject(Event::class.java) ?: continue
                        var classId = event.clss!!.classID
                        firestore.document("User/"+MainActivity.user.email).collection("classes").get().addOnSuccessListener { classes ->
                            doneList.remove(false)
                            classes.forEach { c ->
                                if(c["classID"].toString() == classId){
                                    if(!eventList.contains(event))
                                        eventList.add(event)


                                }

                            }
                            if(doneList.size ==0){
                                reloadEvents()
                            }
                        }
                        doneList.add(false)
                        firestore.document(classId).get().addOnSuccessListener {cls ->
                            doneList.remove(false)
                            val clss = cls.toObject(Class::class.java) ?: return@addOnSuccessListener
                            if(clss.teacher!!.email == MainActivity.user.email){
                                if(!eventList.contains(event))
                                    eventList.add(event)

                            }

                            if(doneList.size ==0){
                                reloadEvents()
                            }
                        }
                    }

//                    recyclerRestaurants.visibility = View.VISIBLE
//                    viewEmpty.visibility = View.GONE

                }
            }

            override fun onError(e: FirebaseFirestoreException) {
                // Show a snackbar on errors
                Log.e("BoardFragment",e.message)
            }
        }

        adapter2= EventMutableListAdapter( this)
       // notificationRecyclerView.adapter = adapter
      /*  var b: Button =  root.findViewById(R.id.button)
        b.setOnClickListener{
            var aa= firestore.collection("messages").document()
        }*/
        return root
    }

    fun reloadEvents(){
        Toast.makeText(requireContext(),eventList.size.toString(),Toast.LENGTH_SHORT).show()
        adapter2.submitList(eventList)
        //adapter2.notifyDataSetChanged()
        notificationRecyclerView.adapter = adapter2
    }

//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        callbacks = context as Callbacks?
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        callbacks = null
//    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?){
        super.onViewCreated(view, savedInstanceState)
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)


       /* homeViewModel.events.observe(
            viewLifecycleOwner,
            Observer { events ->
                events?.let {



                    updateUI(events.toMutableList())

                }
            }
        )*/
       updateUI(homeViewModel.boardRecycleViewItem)


    }

     override fun onStart() {
        super.onStart()

        // Start sign in if necessary
       /* if (shouldStartSignIn()) {
            startSignIn()
            return
        }*/

        // Apply filters
      //  onFilter(viewModel.filters)

        // Start listening for Firestore updates
        adapter.startListening()
    }

     override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onBoardItemSelected(boardItem: DocumentSnapshot) {

    }

    private fun updateUI(crimes: MutableList<BoardRecycleViewItem>) {
      /*  studentAdapter!!.submitList(crimes)
//
      //  studentAdapter = NotificationAdapter(crimes)

        notificationRecyclerView.studentAdapter = studentAdapter*/

    }

  /*  private inner class NotificationHolder(view: View, viewType: Int)
        : RecyclerView.ViewHolder(view),View.OnClickListener {

        private lateinit var board: BoardRecycleViewItem




        val titleTextView: TextView = itemView.findViewById(R.id.notification_title)
        val dateTextView: TextView = itemView.findViewById(R.id.notification_date)




        init {

            if(viewType==0){
                itemView.setOnClickListener(this)

            }
            else {

                *//*var policeButton: Button = itemView.findViewById(R.id.crime_police_button)
                policeButton.setOnClickListener {
                    Toast.makeText(context, "Police Called", Toast.LENGTH_SHORT).show()
                }*//*
                itemView.setOnClickListener(this)
            }


        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(boardRecycleViewItem: BoardRecycleViewItem) {
            this.board = boardRecycleViewItem
            titleTextView.text = this.board.title
            dateTextView.text = this.board.date


         *//*   val sdf = SimpleDateFormat("EEEE,MMM,dd,YYYY HH:mm:ss")
            val d = board.date
            dateTextView.text  = sdf.format(d)*//*

        }





        override fun onClick(v: View?) {
            //callbacks?.onEventSelected(crime.id)
            if(board.type==0) {
                Toast.makeText(context, "YO", Toast.LENGTH_SHORT).show()
//                val newfragment = EventFragment()
//                val fragmentTransaction = fragmentManager!!.beginTransaction()
//                //there is a bug here when user navigates from this fragment directly to another, and then clicking back
//                fragmentTransaction.replace(R.id.nav_host_fragment, newfragment)
//                fragmentTransaction.addToBackStack(null)
//
//                fragmentTransaction.commit()
//                callbacks?.onEventSelected(board.id)


                val bundle = Bundle()
                val id = board.id
                bundle.putInt("eventID", id)
                val eventFrag = EventFragment()
                eventFrag.arguments = bundle
                val fragmentTransaction = fragmentManager!!.beginTransaction()
                fragmentTransaction.replace(R.id.nav_host_fragment, eventFrag)
                fragmentTransaction.commit()
            }
            else {
                Toast.makeText(context, "YOooooooooooo", Toast.LENGTH_SHORT).show()
                homeViewModel.boardRecycleViewItem.remove(board)
                updateUI(homeViewModel.boardRecycleViewItem)
            }

        }

    }

    private inner class NotificationAdapter ()
        : ListAdapter<BoardRecycleViewItem, NotificationHolder>(DiffCallback()){
      //  :RecyclerView.Adapter<NotificationHolder>(){
     *//*   override fun getItemCount(): Int {
         return  events.size
        }
*//*

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onBindViewHolder(holder: NotificationHolder, position: Int) {
               holder.bind(getItem(position))
        *//* //   holder.bind(events.get(position))
            val event = events[position]
            holder . apply {
                titleTextView.text = event.name
                dateTextView . text = event . startDate . toString ()
            }*//*
        }




        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationHolder {

            if (viewType == 0) {
                val view = layoutInflater.inflate(R.layout.list_event_item_board, parent, false)
                return NotificationHolder(view,viewType)
            }
            else{

                val view = layoutInflater.inflate(R.layout.list_notification_item_board, parent, false)
                return NotificationHolder(view,viewType)
            }


        }

        override fun getItemViewType(position: Int): Int {

            return getItem(position).type

        }

    }
    class DiffCallback : DiffUtil.ItemCallback<BoardRecycleViewItem>() {
        override fun areItemsTheSame(oldItem: BoardRecycleViewItem, newItem: BoardRecycleViewItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BoardRecycleViewItem, newItem: BoardRecycleViewItem): Boolean {
            return oldItem == newItem
        }
    }*/

}