/**
  Removing APB instrument from List weblab is not enabled
**/

private void removeABPInstrumentIfNotApplicable(GetAllEligibleInstrumentsResponse getAllEligibleInstrumentsResponse) {
        EligibleInstrumentsResponse eligibleInstrumentsResponse = getAllEligibleInstrumentsResponse
                .getEligibleInstrumentsResponseMap().get(InstrumentType.UPI);
        List<EligibleInstrument> eligibleInstrumentList = upiOperationUtil
                .filterOutAPBHandle(eligibleInstrumentsResponse
                .getEligibleInstruments());
        eligibleInstrumentsResponse.setEligibleInstruments(eligibleInstrumentList);
        getAllEligibleInstrumentsResponse.getEligibleInstrumentsResponseMap()
                .put(InstrumentType.UPI, eligibleInstrumentsResponse);
}


/**
 * Returns filtered list without APB handle.
 * @param upiEligibleInstrumentList List of {@link EligibleInstrument}
 * @return List of {@link EligibleInstrument} filteredPaymentMethods.
 */
public List<EligibleInstrument> filterOutAPBHandle(final List<EligibleInstrument> upiEligibleInstrumentList) {
        return CollectionUtils.emptyIfNull(upiEligibleInstrumentList).stream()
                .filter(this::isAPBHandleValid)
                .collect(Collectors.toList());
}

private boolean isAPBHandleValid(final EligibleInstrument upiEligibleInstrument) {
        final String customerId = HorizonteMacros.getCustomerID();
        final String ap4AndroidBalance20WebLab = releaseManager.getExperimentTreatmentAndRecordTrigger(
                AP4_ANDROID_BALANCE_WEBLAB, SessionId.resolveCurrent(), customerId);
        final String ap4IOSBalance20WebLab = releaseManager.getExperimentTreatmentAndRecordTrigger(AP4_IOS_BALANCE_WEBLAB,
                SessionId.resolveCurrent(), customerId);
        final DeviceOSType deviceOSType = deviceDetectionUtils.getDeviceOSType().orElse(null);
        final UpiInternalCustomerInstrumentDetails upiInternalCustomerInstrumentDetails =
                (UpiInternalCustomerInstrumentDetails) upiEligibleInstrument.getDetails();
        final boolean isIssuingPspAmazon = IssuingPsp.APB.toString().equals(
                upiInternalCustomerInstrumentDetails.getIssuingPsp());
 
        if (DeviceOSType.ANDROID.equals(deviceOSType)) {
            return !isIssuingPspAmazon || isBalance20FeatureWebLabEnabled(ap4AndroidBalance20WebLab,
                    requestContext.getIngressType());
        } else if (DeviceOSType.IOS.equals(deviceOSType)) {
            return !isIssuingPspAmazon || isBalance20FeatureWebLabEnabled(ap4IOSBalance20WebLab,
                    requestContext.getIngressType());
        }
 
        return false;
}


private boolean isBalance20FeatureWebLabEnabled(String balance20WebLab, IngressType ingressType) {
        final boolean isT1TreatmentUseCaseEnabled = !BALANCE_20_T1_TREATMENT_INGRESSES.contains(ingressType);
        final boolean isT2TreatmentUseCaseEnabled = !BALANCE_20_T2_TREATMENT_INGRESSES.contains(ingressType);
 
        final boolean isT1TreatmentApplicable = WEBLAB_TREATMENT_T1.equals(balance20WebLab)
                && isT1TreatmentUseCaseEnabled;
        final boolean isT2TreatmentApplicable = WEBLAB_TREATMENT_T2.equals(balance20WebLab)
                && isT2TreatmentUseCaseEnabled;
        final boolean isT3TreatmentApplicable = WEBLAB_TREATMENT_T3.equals(balance20WebLab);
 
        return isT1TreatmentApplicable || isT2TreatmentApplicable || isT3TreatmentApplicable;
}
