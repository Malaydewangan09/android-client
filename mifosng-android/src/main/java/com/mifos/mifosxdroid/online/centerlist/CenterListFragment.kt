/*
 * This project is licensed under the open source MPL V2.
 * See https://github.com/openMF/android-client/blob/master/LICENSE.md
 */
package com.mifos.mifosxdroid.online.centerlist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import butterknife.ButterKnife
import com.github.therajanmaurya.sweeterror.SweetUIErrorHandler
import com.mifos.mifosxdroid.R
import com.mifos.mifosxdroid.adapters.CentersListAdapter
import com.mifos.mifosxdroid.core.EndlessRecyclerViewScrollListener
import com.mifos.mifosxdroid.core.MifosBaseActivity
import com.mifos.mifosxdroid.core.MifosBaseFragment
import com.mifos.mifosxdroid.core.util.Toaster
import com.mifos.mifosxdroid.databinding.FragmentCentersListBinding
import com.mifos.mifosxdroid.dialogfragments.synccenterdialog.SyncCentersDialogFragment
import com.mifos.mifosxdroid.online.CentersActivity
import com.mifos.mifosxdroid.online.collectionsheet.CollectionSheetFragment
import com.mifos.mifosxdroid.online.createnewcenter.CreateNewCenterFragment
import com.mifos.mifosxdroid.uihelpers.MFDatePicker
import com.mifos.objects.group.Center
import com.mifos.objects.group.CenterWithAssociations
import com.mifos.utils.Constants
import com.mifos.utils.FragmentConstants
import javax.inject.Inject

/**
 * Created by ishankhanna on 11/03/14.
 *
 *
 * CenterListFragment Fetching and Showing CenterList in RecyclerView from
 * >demo.openmf.org/fineract-provider/api/v1/centers?paged=true&offset=0&limit=100>
 */
class CenterListFragment : MifosBaseFragment(), CenterListMvpView, OnRefreshListener {

    private lateinit var binding: FragmentCentersListBinding

    @Inject
    lateinit var mCenterListPresenter: CenterListPresenter


    lateinit var centersListAdapter: CentersListAdapter
    private var centers: List<Center>? = null
    private var selectedCenters: MutableList<Center>? = null
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var actionModeCallback: ActionModeCallback
    private var actionMode: ActionMode? = null
    private lateinit var sweetUIErrorHandler: SweetUIErrorHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        centers = ArrayList()
        selectedCenters = ArrayList()
        actionModeCallback = ActionModeCallback()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCentersListBinding.inflate(inflater, container, false)
        (activity as MifosBaseActivity?)?.activityComponent?.inject(this)
        ButterKnife.bind(this, binding.root)
        mCenterListPresenter.attachView(this)

        //Showing User Interface.
        showUserInterface()

        //Fetching Centers
        /**
         * This is the LoadMore of the RecyclerView. It called When Last Element of RecyclerView
         * will shown on the Screen.
         */
        binding.rvCenterList.addOnScrollListener(object :
            EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int) {
                mCenterListPresenter.loadCenters(true, totalItemsCount)
            }
        })
        mCenterListPresenter.loadCenters(false, 0)
        mCenterListPresenter.loadDatabaseCenters()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabCreateCenter.setOnClickListener {
            onClickCreateNewCenter()
        }
        binding.layoutError.findViewById<Button>(R.id.btn_try_again).setOnClickListener {
            reloadOnError()
        }
    }

    /**
     * This Method is setting the UI
     */
    override fun showUserInterface() {
        setToolbarTitle(resources.getString(R.string.title_activity_centers))
        layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.rvCenterList.layoutManager = layoutManager
        centersListAdapter = CentersListAdapter(
            onCenterClick = { position ->
                if (actionMode != null) {
                    toggleSelection(position)
                } else {
                    val centerIntent = Intent(activity, CentersActivity::class.java)
                    centerIntent.putExtra(Constants.CENTER_ID, centers?.get(position)?.id)
                    startActivity(centerIntent)
                }
            },
            onCenterLongClick = { position ->
                if (actionMode == null)
                    actionMode =
                        (activity as MifosBaseActivity?)?.startSupportActionMode(actionModeCallback)
                toggleSelection(position)
            }
        )
        binding.rvCenterList.setHasFixedSize(true)
        binding.rvCenterList.adapter = centersListAdapter
        binding.swipeContainer.setColorSchemeColors(
            *activity?.resources?.getIntArray(R.array.swipeRefreshColors) ?: IntArray(0)
        )
        binding.swipeContainer.setOnRefreshListener(this)
        sweetUIErrorHandler = SweetUIErrorHandler(activity, binding.root)
    }

    private fun onClickCreateNewCenter() {
        (activity as MifosBaseActivity?)?.replaceFragment(
            CreateNewCenterFragment.newInstance(),
            false, R.id.container_a
        )
    }

    /**
     * This Method will be called, whenever user will pull down to RefreshLayout.
     */
    override fun onRefresh() {
        mCenterListPresenter.loadCenters(false, 0)
        mCenterListPresenter.loadDatabaseCenters()
        if (actionMode != null) actionMode?.finish()
    }

    /**
     * OnClick Error Image icon, reload the centers
     */
    private fun reloadOnError() {
        sweetUIErrorHandler.hideSweetErrorLayoutUI(binding.rvCenterList, binding.layoutError)
        mCenterListPresenter.loadCenters(false, 0)
        mCenterListPresenter.loadDatabaseCenters()
    }

    /**
     * Attaching the this.centers to the CentersListAdapter
     *
     * @param centers List<Center>
    </Center> */
    override fun showCenters(centers: List<Center?>?) {
        this.centers = centers as List<Center>?
        centersListAdapter.setCenters(this.centers ?: emptyList())
        centersListAdapter.notifyDataSetChanged()
    }

    /**
     * Updating the CenterListAdapter
     *
     * @param centers List<Center>
    </Center> */
    override fun showMoreCenters(centers: List<Center?>?) {
        this.centers.addAll()
        centersListAdapter.notifyDataSetChanged()
    }

    /**
     * Showing that Server response is Empty
     *
     * @param message
     */
    override fun showEmptyCenters(message: Int) {
        sweetUIErrorHandler.showSweetEmptyUI(
            getString(R.string.center), getString(message),
            R.drawable.ic_error_black_24dp, binding.rvCenterList, binding.layoutError
        )
    }

    /**
     * This Method for showing simple SeekBar
     *
     * @param message
     */
    override fun showMessage(message: Int) {
        Toaster.show(binding.root, getStringMessage(message))
    }

    /**
     * This Method for showing the CollectionSheet of Center
     *
     * @param centerWithAssociations
     * @param id
     */
    override fun showCentersGroupAndMeeting(
        centerWithAssociations: CenterWithAssociations?,
        id: Int
    ) {
        val mfDatePicker = MFDatePicker()
        mfDatePicker.setOnDatePickListener { date ->
            if (centerWithAssociations?.collectionMeetingCalendar?.id != null) {
                (activity as MifosBaseActivity?)
                    ?.replaceFragment(
                        CollectionSheetFragment.newInstance(
                            id, date,
                            centerWithAssociations.collectionMeetingCalendar.id
                        ),
                        true, R.id.container
                    )
            } else {
                showMessage(R.string.no_meeting_found)
            }
        }
        mfDatePicker.show(requireActivity().supportFragmentManager, MFDatePicker.TAG)
    }

    /**
     * If Loading Centers is failed on first request then show to user a message that center failed
     * to load.
     */
    override fun showFetchingError() {
        val errorMessage = getStringMessage(R.string.failed_to_fetch_centers)
        sweetUIErrorHandler.showSweetErrorUI(
            errorMessage,
            R.drawable.ic_error_black_24dp, binding.rvCenterList, binding.layoutError
        )
    }

    /**
     * This Method for showing Progress bar if the Center count is zero otherwise
     * shows swipeRefreshLayout
     *
     * @param show Boolean
     */
    override fun showProgressbar(show: Boolean) {
        binding.swipeContainer.isRefreshing = show
        if (show && centersListAdapter.itemCount == 0) {
            binding.progressbarCenter.visibility = View.VISIBLE
            binding.swipeContainer.isRefreshing = false
        } else {
            binding.progressbarCenter.visibility = View.GONE
        }
    }

    /**
     * This Method unregister the RecyclerView OnScrollListener and SwipeRefreshLayout
     * and NoClientIcon click event.
     */
    override fun unregisterSwipeAndScrollListener() {
        binding.rvCenterList.clearOnScrollListeners()
        binding.swipeContainer.isEnabled = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.mItem_search) requireActivity().finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mCenterListPresenter.detachView()
    }

    /**
     * Toggle the selection state of an item.
     *
     *
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private fun toggleSelection(position: Int) {
        centersListAdapter.toggleSelection(position)
        val count = centersListAdapter.selectedItemCount
        if (count == 0) {
            actionMode?.finish()
        } else {
            actionMode?.title = count.toString()
            actionMode?.invalidate()
        }
    }

    /**
     * This ActionModeCallBack Class handling the User Event after the Selection of Clients. Like
     * Click of Menu Sync Button and finish the ActionMode
     */
    private inner class ActionModeCallback : ActionMode.Callback {
        private val LOG_TAG = ActionModeCallback::class.java.simpleName
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_sync, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.action_sync -> {
                    selectedCenters?.clear()
                    for (position in centersListAdapter.selectedItems) {
                        val center = centers?.get(position)
                        if (center != null) {
                            selectedCenters?.add(center)
                        }
                    }
                    val syncCentersDialogFragment =
                        SyncCentersDialogFragment.newInstance(selectedCenters)
                    val fragmentTransaction = activity
                        ?.supportFragmentManager?.beginTransaction()
                    fragmentTransaction?.addToBackStack(FragmentConstants.FRAG_CLIENT_SYNC)
                    syncCentersDialogFragment.isCancelable = false
                    if (fragmentTransaction != null) {
                        syncCentersDialogFragment.show(
                            fragmentTransaction,
                            resources.getString(R.string.sync_centers)
                        )
                    }
                    mode.finish()
                    true
                }

                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            centersListAdapter.clearSelection()
            actionMode = null
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): CenterListFragment {
            val centerListFragment = CenterListFragment()
            val args = Bundle()
            centerListFragment.arguments = args
            return centerListFragment
        }
    }
}

private fun <E> List<E>?.addAll() {

}
