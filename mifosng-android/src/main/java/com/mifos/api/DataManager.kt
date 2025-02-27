package com.mifos.api

import com.mifos.api.datamanager.DataManagerClient
import com.mifos.api.model.CollectionSheetPayload
import com.mifos.api.model.Payload
import com.mifos.objects.accounts.loan.LoanApproval
import com.mifos.objects.accounts.loan.LoanWithAssociations
import com.mifos.objects.accounts.loan.Loans
import com.mifos.objects.client.ChargeCreationResponse
import com.mifos.objects.client.Charges
import com.mifos.objects.client.Page
import com.mifos.objects.db.CollectionSheet
import com.mifos.objects.db.OfflineCenter
import com.mifos.objects.group.Center
import com.mifos.objects.group.CenterWithAssociations
import com.mifos.objects.group.Group
import com.mifos.objects.group.GroupWithAssociations
import com.mifos.objects.organisation.LoanProducts
import com.mifos.objects.organisation.Office
import com.mifos.objects.organisation.Staff
import com.mifos.objects.response.SaveResponse
import com.mifos.objects.templates.clients.ChargeTemplate
import com.mifos.objects.templates.loans.GroupLoanTemplate
import com.mifos.services.data.ChargesPayload
import com.mifos.services.data.GroupLoanPayload
import okhttp3.ResponseBody
import rx.Observable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Rajan Maurya on 4/6/16.
 */
@Singleton
class DataManager {
    private val mBaseApiManager: BaseApiManager
    private var mDataManagerClient: DataManagerClient? = null

    //TODO : This Constructor is temp after splitting the Datamanager layer into Sub DataManager
    constructor(baseApiManager: BaseApiManager) {
        mBaseApiManager = baseApiManager
    }

    @Inject
    constructor(
        baseApiManager: BaseApiManager,
        dataManagerClient: DataManagerClient?
    ) {
        mBaseApiManager = baseApiManager
        mDataManagerClient = dataManagerClient
    }

    /**
     * Center API
     */
    fun getGroupsByCenter(id: Int): Observable<CenterWithAssociations> {
        return mBaseApiManager.centerApi.getAllGroupsForCenter(id)
    }

    fun getCentersInOffice(id: Int, params: Map<String?, Any?>?): Observable<MutableList<Center>> {
        return mBaseApiManager.centerApi.getAllCentersInOffice(id, params)
    }

    fun getCollectionSheet(id: Long, payload: Payload?): Observable<CollectionSheet> {
        return mBaseApiManager.centerApi.getCollectionSheet(id, payload)
    }

    fun saveCollectionSheet(
        centerId: Int,
        collectionSheetPayload: CollectionSheetPayload?
    ): Observable<SaveResponse> {
        return mBaseApiManager.centerApi.saveCollectionSheet(
            centerId, collectionSheetPayload
        )
    }

    fun saveCollectionSheetAsync(
        id: Int,
        payload: CollectionSheetPayload?
    ): Observable<SaveResponse> {
        return mBaseApiManager.centerApi.saveCollectionSheetAsync(id, payload)
    }

    fun getCenterList(
        dateFormat: String?, locale: String?, meetingDate: String?, officeId: Int, staffId: Int
    ): Observable<MutableList<OfflineCenter>> {
        return mBaseApiManager.centerApi.getCenterList(
            dateFormat, locale, meetingDate,
            officeId, staffId
        )
    }

    /**
     * Charges API
     */
    //TODO Remove this Method After fixing the Charge Test
    fun getClientCharges(clientId: Int, offset: Int, limit: Int): Observable<Page<Charges>> {
        return mBaseApiManager.chargeApi.getListOfCharges(clientId, offset, limit)
    }

    fun getAllChargesV2(clientId: Int): Observable<ChargeTemplate> {
        return mBaseApiManager.chargeApi.getAllChargesS(clientId)
    }

    fun createCharges(
        clientId: Int,
        payload: ChargesPayload?
    ): Observable<ChargeCreationResponse> {
        return mBaseApiManager.chargeApi.createCharges(clientId, payload)
    }

    fun getAllChargesV3(loanId: Int): Observable<ResponseBody> {
        return mBaseApiManager.chargeApi.getAllChargev3(loanId)
    }

    fun createLoanCharges(
        loanId: Int,
        chargesPayload: ChargesPayload?
    ): Observable<ChargeCreationResponse> {
        return mBaseApiManager.chargeApi.createLoanCharges(loanId, chargesPayload)
    }

    /**
     * Groups API
     */
    fun getGroups(groupid: Int): Observable<GroupWithAssociations> {
        return mBaseApiManager.groupApi.getGroupWithAssociations(groupid)
    }

    fun getGroupsByOffice(
        office: Int,
        params: Map<String?, Any?>?
    ): Observable<MutableList<Group>> {
        return mBaseApiManager.groupApi.getAllGroupsInOffice(office, params)
    }

    /**
     * Offices API
     */
    val offices: Observable<MutableList<Office>>
        get() = mBaseApiManager.officeApi.allOffices

    /**
     * Staff API
     */
    fun getStaffInOffice(officeId: Int): Observable<MutableList<Staff>> {
        return mBaseApiManager.staffApi.getStaffForOffice(officeId)
    }

    val allStaff: Observable<MutableList<Staff>>
        get() = mBaseApiManager.staffApi.allStaff

    /**
     * Loans API
     */
    fun getLoanTransactions(loan: Int): Observable<LoanWithAssociations> {
        return mBaseApiManager.loanApi.getLoanWithTransactions(loan)
    }

    val allLoans: Observable<MutableList<LoanProducts>>
        get() = mBaseApiManager.loanApi.allLoans

    fun getGroupLoansAccountTemplate(groupId: Int, productId: Int): Observable<GroupLoanTemplate> {
        return mBaseApiManager.loanApi.getGroupLoansAccountTemplate(groupId, productId)
    }

    fun createGroupLoansAccount(loansPayload: GroupLoanPayload?): Observable<Loans> {
        return mBaseApiManager.loanApi.createGroupLoansAccount(loansPayload)
    }

    fun getLoanRepaySchedule(loanId: Int): Observable<LoanWithAssociations> {
        return mBaseApiManager.loanApi.getLoanRepaymentSchedule(loanId)
    }

    fun approveLoan(loanId: Int, loanApproval: LoanApproval?): Observable<GenericResponse> {
        return mBaseApiManager.loanApi.approveLoanApplication(loanId, loanApproval)
    }

    fun getListOfLoanCharges(loanId: Int): Observable<MutableList<Charges>> {
        return mBaseApiManager.loanApi.getListOfLoanCharges(loanId)
    }

    fun getListOfCharges(clientId: Int): Observable<Page<Charges>> {
        return mBaseApiManager.loanApi.getListOfCharges(clientId)
    }
}