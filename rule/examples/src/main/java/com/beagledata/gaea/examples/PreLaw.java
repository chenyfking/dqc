package com.beagledata.gaea.examples;

import java.io.Serializable;

/**
 * 司法公安税务
 *
 * Created by liulu on 2018/11/5.
 */
public class PreLaw implements Serializable {
    private static final long serialVersionUID = 5158558527029049618L;

    /**
     * 借款人身份证_姓名命中法院失信模糊名单
     */
    private Boolean cardNameHitCourtDiscreditVague;
    /**
     * 借款人身份证_姓名命中法院执行模糊名单
     */
    private Boolean cardNameHitCourtExeVague;
    /**
     * 借款人身份证_姓名命中法院结案模糊名单
     */
    private Boolean cardNameHitCourtSettleCaseVague;
    /**
     * 借款人身份证_姓名命中羊毛党嫌疑名单
     */
    private Boolean cardNameHitEconnoisseur;
    /**
     * 借款人身份证_姓名命中以贷养贷嫌疑名单
     */
    private Boolean cardNameHitYdyd;
    /**
     * 借款人身份证命中法院失信名单
     */
    private Boolean idCardHitCourtDiscredit;
    /**
     * 借款人身份证命中法院执行名单
     */
    private Boolean idCardHitCourtExecute;
    /**
     * 借款人身份证命中法院结案名单
     */
    private Boolean idCardHitCourtSettleCase;
    /**
     * 借款人身份证命中犯罪通缉名单
     */
    private Boolean idCardHitCrimeWanted;
    /**
     * 借款人身份证命中欠税名单
     */
    private Boolean idCardHitDissTax;
    /**
     * 借款人身份证命中欠税公司法人代表名单
     */
    private Boolean idCardHitOfficialDissTax;
    /**
     * 借款人手机号命中欠款公司法人代表
     */
    private Boolean phoneHitOfficialDissMoney;

    public Boolean getCardNameHitCourtDiscreditVague() {
        return cardNameHitCourtDiscreditVague;
    }

    public void setCardNameHitCourtDiscreditVague(Boolean cardNameHitCourtDiscreditVague) {
        this.cardNameHitCourtDiscreditVague = cardNameHitCourtDiscreditVague;
    }

    public Boolean getCardNameHitCourtExeVague() {
        return cardNameHitCourtExeVague;
    }

    public void setCardNameHitCourtExeVague(Boolean cardNameHitCourtExeVague) {
        this.cardNameHitCourtExeVague = cardNameHitCourtExeVague;
    }

    public Boolean getCardNameHitCourtSettleCaseVague() {
        return cardNameHitCourtSettleCaseVague;
    }

    public void setCardNameHitCourtSettleCaseVague(Boolean cardNameHitCourtSettleCaseVague) {
        this.cardNameHitCourtSettleCaseVague = cardNameHitCourtSettleCaseVague;
    }

    public Boolean getCardNameHitEconnoisseur() {
        return cardNameHitEconnoisseur;
    }

    public void setCardNameHitEconnoisseur(Boolean cardNameHitEconnoisseur) {
        this.cardNameHitEconnoisseur = cardNameHitEconnoisseur;
    }

    public Boolean getCardNameHitYdyd() {
        return cardNameHitYdyd;
    }

    public void setCardNameHitYdyd(Boolean cardNameHitYdyd) {
        this.cardNameHitYdyd = cardNameHitYdyd;
    }

    public Boolean getIdCardHitCourtDiscredit() {
        return idCardHitCourtDiscredit;
    }

    public void setIdCardHitCourtDiscredit(Boolean idCardHitCourtDiscredit) {
        this.idCardHitCourtDiscredit = idCardHitCourtDiscredit;
    }

    public Boolean getIdCardHitCourtExecute() {
        return idCardHitCourtExecute;
    }

    public void setIdCardHitCourtExecute(Boolean idCardHitCourtExecute) {
        this.idCardHitCourtExecute = idCardHitCourtExecute;
    }

    public Boolean getIdCardHitCourtSettleCase() {
        return idCardHitCourtSettleCase;
    }

    public void setIdCardHitCourtSettleCase(Boolean idCardHitCourtSettleCase) {
        this.idCardHitCourtSettleCase = idCardHitCourtSettleCase;
    }

    public Boolean getIdCardHitCrimeWanted() {
        return idCardHitCrimeWanted;
    }

    public void setIdCardHitCrimeWanted(Boolean idCardHitCrimeWanted) {
        this.idCardHitCrimeWanted = idCardHitCrimeWanted;
    }

    public Boolean getIdCardHitDissTax() {
        return idCardHitDissTax;
    }

    public void setIdCardHitDissTax(Boolean idCardHitDissTax) {
        this.idCardHitDissTax = idCardHitDissTax;
    }

    public Boolean getIdCardHitOfficialDissTax() {
        return idCardHitOfficialDissTax;
    }

    public void setIdCardHitOfficialDissTax(Boolean idCardHitOfficialDissTax) {
        this.idCardHitOfficialDissTax = idCardHitOfficialDissTax;
    }

    public Boolean getPhoneHitOfficialDissMoney() {
        return phoneHitOfficialDissMoney;
    }

    public void setPhoneHitOfficialDissMoney(Boolean phoneHitOfficialDissMoney) {
        this.phoneHitOfficialDissMoney = phoneHitOfficialDissMoney;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PreLaw{");
        sb.append("cardNameHitCourtDiscreditVague=").append(cardNameHitCourtDiscreditVague);
        sb.append(", cardNameHitCourtExeVague=").append(cardNameHitCourtExeVague);
        sb.append(", cardNameHitCourtSettleCaseVague=").append(cardNameHitCourtSettleCaseVague);
        sb.append(", cardNameHitEconnoisseur=").append(cardNameHitEconnoisseur);
        sb.append(", cardNameHitYdyd=").append(cardNameHitYdyd);
        sb.append(", idCardHitCourtDiscredit=").append(idCardHitCourtDiscredit);
        sb.append(", idCardHitCourtExecute=").append(idCardHitCourtExecute);
        sb.append(", idCardHitCourtSettleCase=").append(idCardHitCourtSettleCase);
        sb.append(", idCardHitCrimeWanted=").append(idCardHitCrimeWanted);
        sb.append(", idCardHitDissTax=").append(idCardHitDissTax);
        sb.append(", idCardHitOfficialDissTax=").append(idCardHitOfficialDissTax);
        sb.append(", phoneHitOfficialDissMoney=").append(phoneHitOfficialDissMoney);
        sb.append('}');
        return sb.toString();
    }
}
