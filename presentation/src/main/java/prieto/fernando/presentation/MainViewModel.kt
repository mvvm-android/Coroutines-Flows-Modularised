package prieto.fernando.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import prieto.fernando.core.presentation.BaseViewModel
import prieto.fernando.presentation.model.CompanyInfoUiModel
import prieto.fernando.presentation.model.LaunchUiModel
import timber.log.Timber
import javax.inject.Inject

interface MainViewModelInputs {
    fun launches(yearFilterCriteria: Int = -1, ascendantOrder: Boolean = false)
    fun companyInfo()
    fun openLink(link: String)
    fun onFilterClicked()
}

class MainViewModel @Inject constructor(
    private val getLaunches: GetLaunches,
    private val getCompanyInfo: GetCompanyInfo
) : BaseViewModel(), MainViewModelInputs {

    private val launchUiModelRetrieved: MutableLiveData<List<LaunchUiModel>> = MutableLiveData()
    private val companyInfoUiModelRetrieved: MutableLiveData<CompanyInfoUiModel> = MutableLiveData()
    private val loadingBody: MutableLiveData<Boolean> = MutableLiveData()
    private val loadingHeader: MutableLiveData<Boolean> = MutableLiveData()
    private val headerError: MutableLiveData<Unit> = MutableLiveData()
    private val bodyError: MutableLiveData<Unit> = MutableLiveData()
    private val onOpenLink: MutableLiveData<String> = MutableLiveData()
    private val onShowDialog: MutableLiveData<Unit> = MutableLiveData()

    fun headerError(): LiveData<Unit> = headerError
    fun bodyError(): LiveData<Unit> = bodyError
    fun loadingBody(): LiveData<Boolean> = loadingBody
    fun loadingHeader(): LiveData<Boolean> = loadingHeader
    fun onLaunchesUiModelRetrieved(): LiveData<List<LaunchUiModel>> = launchUiModelRetrieved
    fun onCompanyInfoUiModelRetrieved(): LiveData<CompanyInfoUiModel> = companyInfoUiModelRetrieved
    fun onOpenLink(): LiveData<String> = onOpenLink
    fun onShowDialog(): LiveData<Unit> = onShowDialog

    override fun launches(yearFilterCriteria: Int, ascendantOrder: Boolean) {
        getLaunches.execute(yearFilterCriteria, ascendantOrder)
            .compose(schedulerProvider.doOnIoObserveOnMainSingle())
            .doOnSubscribe { loadingBody.postValue(true) }
            .doFinally { loadingBody.postValue(false) }
            .subscribe({ launchesUiModel ->
                launchUiModelRetrieved.postValue(launchesUiModel)
            }, { throwable ->
                Timber.d(throwable)
                bodyError.postValue(Unit)
            }).also { subscriptions.add(it) }
    }

    override fun companyInfo() {
        getCompanyInfo.execute()
            .compose(schedulerProvider.doOnIoObserveOnMainSingle())
            .doOnSubscribe { loadingHeader.postValue(true) }
            .doFinally { loadingHeader.postValue(false) }
            .subscribe({ companyInfo ->
                companyInfoUiModelRetrieved.postValue(companyInfo)
            }, { throwable ->
                Timber.d(throwable)
                headerError.postValue(Unit)
            }).also { subscriptions.add(it) }
    }

    override fun openLink(link: String) {
        onOpenLink.postValue(link)
    }

    override fun onFilterClicked() {
        onShowDialog.postValue(Unit)
    }
}