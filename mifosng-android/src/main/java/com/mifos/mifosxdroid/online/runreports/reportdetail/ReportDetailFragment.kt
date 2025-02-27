package com.mifos.mifosxdroid.online.runreports.reportdetail

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import com.mifos.mifosxdroid.R
import com.mifos.mifosxdroid.core.MifosBaseActivity
import com.mifos.mifosxdroid.core.MifosBaseFragment
import com.mifos.mifosxdroid.core.util.Toaster.show
import com.mifos.mifosxdroid.online.runreports.report.ReportFragment
import com.mifos.mifosxdroid.uihelpers.MFDatePicker
import com.mifos.mifosxdroid.uihelpers.MFDatePicker.OnDatePickListener
import com.mifos.objects.runreports.FullParameterListResponse
import com.mifos.objects.runreports.client.ClientReportTypeItem
import com.mifos.utils.Constants
import com.mifos.utils.FragmentConstants
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

/**
 * Created by Tarun on 04-08-17.
 */
class ReportDetailFragment : MifosBaseFragment(), ReportDetailMvpView, OnDatePickListener {
    @JvmField
    @BindView(R.id.tv_report_name)
    var tvReportName: TextView? = null

    @JvmField
    @BindView(R.id.tv_report_type)
    var tvReportType: TextView? = null

    @JvmField
    @BindView(R.id.tv_report_category)
    var tvReportCategory: TextView? = null

    @JvmField
    @BindView(R.id.table_details)
    var tableDetails: TableLayout? = null

    @JvmField
    @Inject
    var presenter: ReportDetailPresenter? = null
    private lateinit var rootView: View
    private var reportItem: ClientReportTypeItem? = null
    private var fetchLoanOfficer = false
    private var fetchLoanProduct = false
    private var fundMap: HashMap<String, Int>? = null
    private var loanOfficerMap: HashMap<String, Int>? = null
    private var loanProductMap: HashMap<String, Int>? = null
    private var loanPurposeMap: HashMap<String, Int>? = null
    private var officeMap: HashMap<String, Int>? = null
    private var parMap: HashMap<String, Int>? = null
    private var subStatusMap: HashMap<String, Int>? = null
    private var glAccountNoMap: HashMap<String, Int>? = null
    private var obligDateTypeMap: HashMap<String, Int>? = null
    private var currencyMap: HashMap<String, String>? = null
    private var dateField: String? = null
    var datePicker: DialogFragment? = null
    private var tvField: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as MifosBaseActivity).activityComponent?.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_client_report_details, container, false)
        setHasOptionsMenu(true)
        ButterKnife.bind(this, rootView)
        presenter!!.attachView(this)
        reportItem = requireArguments().getParcelable(Constants.CLIENT_REPORT_ITEM)
        setUpUi()
        return rootView
    }

    private fun setUpUi() {
        tvReportName!!.text = reportItem!!.reportName
        tvReportCategory!!.text = reportItem!!.reportCategory
        tvReportType!!.text = reportItem!!.reportType
        val reportName = "'" + reportItem!!.reportName + "'"
        presenter!!.fetchFullParameterList(reportName, true)
        datePicker = MFDatePicker.newInsance(this)
    }

    private fun addTableRow(data: FullParameterListResponse, identifier: String) {
        val row = TableRow(context)
        val rowParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rowParams.gravity = Gravity.CENTER
        rowParams.setMargins(0, 0, 0, 10)
        row.layoutParams = rowParams
        val tvLabel = TextView(context)
        row.addView(tvLabel)
        val spinner = Spinner(context)
        row.addView(spinner)
        val spinnerValues = ArrayList<String>()
        when (identifier) {
            Constants.LOAN_OFFICER_ID_SELECT -> {
                spinner.tag = Constants.R_LOAN_OFFICER_ID
                loanOfficerMap = presenter!!.filterIntHashMapForSpinner(
                    data.data,
                    spinnerValues
                )
                tvLabel.text = getString(R.string.loan_officer)
            }

            Constants.LOAN_PRODUCT_ID_SELECT -> {
                spinner.tag = Constants.R_LOAN_PRODUCT_ID
                loanProductMap = presenter!!.filterIntHashMapForSpinner(
                    data.data,
                    spinnerValues
                )
                tvLabel.text = getString(R.string.loanproduct)
            }

            Constants.LOAN_PURPOSE_ID_SELECT -> {
                spinner.tag = Constants.R_LOAN_PURPOSE_ID
                loanPurposeMap = presenter!!.filterIntHashMapForSpinner(
                    data.data,
                    spinnerValues
                )
                tvLabel.text = getString(R.string.report_loan_purpose)
            }

            Constants.FUND_ID_SELECT -> {
                spinner.tag = Constants.R_FUND_ID
                fundMap = presenter!!.filterIntHashMapForSpinner(data.data, spinnerValues)
                tvLabel.text = getString(R.string.loan_fund)
            }

            Constants.CURRENCY_ID_SELECT -> {
                spinner.tag = Constants.R_CURRENCY_ID
                currencyMap = presenter!!.filterStringHashMapForSpinner(
                    data.data,
                    spinnerValues
                )
                tvLabel.text = getString(R.string.currency)
            }

            Constants.OFFICE_ID_SELECT -> {
                spinner.tag = Constants.R_OFFICE_ID
                officeMap = presenter!!.filterIntHashMapForSpinner(data.data, spinnerValues)
                tvLabel.text = getString(R.string.office)
            }

            Constants.PAR_TYPE_SELECT -> {
                spinner.tag = Constants.R_PAR_TYPE
                parMap = presenter!!.filterIntHashMapForSpinner(data.data, spinnerValues)
                tvLabel.text = getString(R.string.par_calculation)
            }

            Constants.SAVINGS_ACCOUNT_SUB_STATUS -> {
                spinner.tag = Constants.R_SUB_STATUS
                subStatusMap = presenter!!.filterIntHashMapForSpinner(data.data, spinnerValues)
                tvLabel.text = getString(R.string.savings_acc_deposit)
            }

            Constants.SELECT_GL_ACCOUNT_NO -> {
                spinner.tag = Constants.R_ACCOUNT
                glAccountNoMap = presenter!!.filterIntHashMapForSpinner(data.data, spinnerValues)
                tvLabel.text = getString(R.string.glaccount)
            }

            Constants.OBLIG_DATE_TYPE_SELECT -> {
                spinner.tag = Constants.R_OBLIG_DATE_TYPE
                obligDateTypeMap = presenter!!.filterIntHashMapForSpinner(data.data, spinnerValues)
                tvLabel.text = getString(R.string.obligation_date_type)
            }
        }
        val adapter: ArrayAdapter<*> = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item, spinnerValues
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
                if (spinner.tag.toString() == Constants.R_OFFICE_ID && fetchLoanOfficer) {
                    val officeId = officeMap!![spinner.selectedItem.toString()]!!
                    presenter!!.fetchOffices(Constants.LOAN_OFFICER_ID_SELECT, officeId, true)
                } else if (spinner.tag.toString() == Constants.R_CURRENCY_ID && fetchLoanProduct) {
                    val currencyId = currencyMap!![spinner.selectedItem.toString()]
                    presenter!!.fetchProduct(Constants.LOAN_PRODUCT_ID_SELECT, currencyId, true)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        tableDetails!!.addView(row)
    }

    private fun runReport() {
        if (tableDetails!!.childCount < 1) {
            show(rootView, getString(R.string.msg_report_empty))
        } else {
            var fundId: Int?
            var loanOfficeId: Int?
            var loanProductId: Int?
            var loanPurposeId: Int?
            var officeId: Int?
            var parId: Int?
            var subId: Int?
            var obligId: Int?
            var glAccountId: Int?
            var currencyId: String?
            val map: MutableMap<String, String?> = HashMap()

            /* There are variable number of parameters in the request query.
              Hence, create a Map instead of hardcoding the number of
              query parameters in the Retrofit Service.*/for (i in 0 until tableDetails!!.childCount) {
                val tableRow = tableDetails!!.getChildAt(i) as TableRow
                if (tableRow.getChildAt(1) is Spinner) {
                    val sp = tableRow.getChildAt(1) as Spinner
                    when (sp.tag.toString()) {
                        Constants.R_LOAN_OFFICER_ID -> {
                            loanOfficeId = loanOfficerMap!![sp.selectedItem.toString()]
                            if (loanOfficeId != -1) {
                                map[sp.tag.toString()] = loanOfficeId.toString()
                            }
                        }

                        Constants.R_LOAN_PRODUCT_ID -> {
                            loanProductId = loanProductMap!![sp.selectedItem.toString()]
                            if (loanProductId != -1) {
                                map[sp.tag.toString()] = loanProductId.toString()
                            }
                        }

                        Constants.R_LOAN_PURPOSE_ID -> {
                            loanPurposeId = loanPurposeMap!![sp.selectedItem.toString()]
                            if (loanPurposeId != -1) {
                                map[sp.tag.toString()] = loanPurposeId.toString()
                            }
                        }

                        Constants.R_FUND_ID -> {
                            fundId = fundMap!![sp.selectedItem.toString()]
                            if (fundId != -1) {
                                map[sp.tag.toString()] = fundId.toString()
                            }
                        }

                        Constants.R_CURRENCY_ID -> {
                            currencyId = currencyMap!![sp.selectedItem.toString()]
                            if (currencyId != "") {
                                map[sp.tag.toString()] = currencyId
                            }
                        }

                        Constants.R_OFFICE_ID -> {
                            officeId = officeMap!![sp.selectedItem.toString()]
                            if (officeId != -1) {
                                map[sp.tag.toString()] = officeId.toString()
                            }
                        }

                        Constants.R_PAR_TYPE -> {
                            parId = parMap!![sp.selectedItem.toString()]
                            if (parId != -1) {
                                map[sp.tag.toString()] = parId.toString()
                            }
                        }

                        Constants.R_ACCOUNT -> {
                            glAccountId = glAccountNoMap!![sp.selectedItem.toString()]
                            if (glAccountId != -1) {
                                map[sp.tag.toString()] = glAccountId.toString()
                            }
                        }

                        Constants.R_SUB_STATUS -> {
                            subId = subStatusMap!![sp.selectedItem.toString()]
                            if (subId != -1) {
                                map[sp.tag.toString()] = subId.toString()
                            }
                        }

                        Constants.R_OBLIG_DATE_TYPE -> {
                            obligId = obligDateTypeMap!![sp.selectedItem.toString()]
                            if (obligId != -1) {
                                map[sp.tag.toString()] = obligId.toString()
                            }
                        }
                    }
                } else if (tableRow.getChildAt(1) is EditText) {
                    val et = tableRow.getChildAt(1) as EditText
                    map[et.tag.toString()] = et.text.toString()
                }
            }
            presenter!!.fetchRunReportWithQuery(reportItem!!.reportName, map)
        }
    }

    override fun showRunReport(response: FullParameterListResponse) {
        val bundle = Bundle()
        bundle.putParcelable(Constants.REPORT_NAME, response)
        val fragmentTransaction = requireActivity()
            .supportFragmentManager.beginTransaction()
        fragmentTransaction.addToBackStack("ClientDetails")
        fragmentTransaction.replace(R.id.container, ReportFragment.newInstance(bundle))
            .commit()
    }

    override fun showOffices(response: FullParameterListResponse, identifier: String) {
        for (i in 0 until tableDetails!!.childCount) {
            val tableRow = tableDetails!!.getChildAt(i) as TableRow
            if (tableRow.getChildAt(1) is EditText) {
                continue
            }
            val sp = tableRow.getChildAt(1) as Spinner
            if (sp.tag.toString() == Constants.R_LOAN_OFFICER_ID) {
                val spinnerValues = ArrayList<String>()
                loanOfficerMap = presenter!!.filterIntHashMapForSpinner(
                    response.data,
                    spinnerValues
                )
                val adapter: ArrayAdapter<*> = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item, spinnerValues
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                sp.adapter = adapter
                return
            }
        }
        addTableRow(response, identifier)
    }

    override fun showProduct(response: FullParameterListResponse, identifier: String) {
        for (i in 0 until tableDetails!!.childCount) {
            val tableRow = tableDetails!!.getChildAt(i) as TableRow
            if (tableRow.getChildAt(1) is EditText) {
                continue
            }
            val sp = tableRow.getChildAt(1) as Spinner
            if (sp.tag.toString() == Constants.R_LOAN_PRODUCT_ID) {
                val spinnerValues = ArrayList<String>()
                loanProductMap = presenter!!.filterIntHashMapForSpinner(
                    response.data,
                    spinnerValues
                )
                val adapter: ArrayAdapter<*> = ArrayAdapter(
                    requireActivity(),
                    android.R.layout.simple_spinner_item, spinnerValues
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                sp.adapter = adapter
                return
            }
        }
        addTableRow(response, identifier)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_runreport, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_run_report -> runReport()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showError(error: String) {
        show(rootView, error)
    }

    override fun showFullParameterResponse(response: FullParameterListResponse) {
        for (row in response.data!!) {
            when (row.row[0]) {
                Constants.LOAN_OFFICER_ID_SELECT -> fetchLoanOfficer = true
                Constants.LOAN_PRODUCT_ID_SELECT -> fetchLoanProduct = true
                Constants.START_DATE_SELECT -> addTextView(Constants.START_DATE_SELECT)
                Constants.END_DATE_SELECT -> addTextView(Constants.END_DATE_SELECT)
                Constants.SELECT_ACCOUNT -> addTextView(Constants.SELECT_ACCOUNT)
                Constants.FROM_X_SELECT -> addTextView(Constants.FROM_X_SELECT)
                Constants.TO_Y_SELECT -> addTextView(Constants.TO_Y_SELECT)
                Constants.OVERDUE_X_SELECT -> addTextView(Constants.OVERDUE_X_SELECT)
                Constants.OVERDUE_Y_SELECT -> addTextView(Constants.OVERDUE_Y_SELECT)
            }
            presenter!!.fetchParameterDetails(row.row[0], true)
        }
    }

    private fun addTextView(identifier: String) {
        val row = TableRow(context)
        val rowParams = TableRow.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rowParams.gravity = Gravity.CENTER
        rowParams.setMargins(0, 0, 0, 10)
        row.layoutParams = rowParams
        val tvLabel = TextView(context)
        row.addView(tvLabel)
        tvField = EditText(context)
        row.addView(tvField)
        when (identifier) {
            Constants.START_DATE_SELECT -> {
                tvField!!.tag = Constants.R_START_DATE
                tvLabel.text = getString(R.string.start_date)
            }

            Constants.END_DATE_SELECT -> {
                tvField!!.tag = Constants.R_END_DATE
                tvLabel.text = getString(R.string.end_date)
            }

            Constants.SELECT_ACCOUNT -> {
                tvField!!.tag = Constants.R_ACCOUNT_NO
                tvLabel.text = getString(R.string.enter_account_no)
            }

            Constants.FROM_X_SELECT -> {
                tvField!!.tag = Constants.R_FROM_X
                tvLabel.text = getString(R.string.from_x_number)
            }

            Constants.TO_Y_SELECT -> {
                tvField!!.tag = Constants.R_TO_Y
                tvLabel.text = getString(R.string.to_y_number)
            }

            Constants.OVERDUE_X_SELECT -> {
                tvField!!.tag = Constants.R_OVERDUE_X
                tvLabel.text = getString(R.string.overdue_x_number)
            }

            Constants.OVERDUE_Y_SELECT -> {
                tvField!!.tag = Constants.R_OVERDUE_Y
                tvLabel.text = getString(R.string.overdue_y_number)
            }
        }
        tvField!!.isFocusable = false
        tvField!!.setOnClickListener { v ->
            dateField = v.tag.toString()
            if (dateField == Constants.R_START_DATE || dateField == Constants.R_END_DATE) {
                datePicker!!.show(
                    requireActivity().supportFragmentManager,
                    FragmentConstants.DFRAG_DATE_PICKER
                )
            }
        }
        tableDetails!!.addView(row)
    }

    override fun showParameterDetails(response: FullParameterListResponse, identifier: String) {
        addTableRow(response, identifier)
    }

    override fun showProgressbar(b: Boolean) {
        if (b) {
            showMifosProgressDialog()
        } else {
            hideMifosProgressDialog()
        }
    }

    override fun onDatePicked(date: String) {
        for (i in 0 until tableDetails!!.childCount) {
            val tableRow = tableDetails!!.getChildAt(i) as TableRow
            if (tableRow.getChildAt(1) is Spinner) {
                continue
            }
            val et = tableRow.getChildAt(1) as EditText
            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy")
            var dateModified: Date? = null
            try {
                dateModified = simpleDateFormat.parse(date)
            } catch (e: ParseException) {
            }
            val simpleDateFormat1 = SimpleDateFormat("yyyy-MM-dd")
            if (et.tag.toString() == dateField) {
                et.setText(simpleDateFormat1.format(dateModified))
                break
            }
        }
    }

    companion object {
        fun newInstance(args: Bundle?): ReportDetailFragment {
            val fragment = ReportDetailFragment()
            fragment.arguments = args
            return fragment
        }
    }
}