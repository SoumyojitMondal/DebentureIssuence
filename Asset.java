package net.corda.samples.states;

import net.corda.core.contracts.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.samples.contracts.AssetContract;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;

@BelongsToContract(AssetContract.class)
public class Asset implements LinearState{

    private final String assetId;
    private final String assetType;
    private final int assetValue;
    private final Party createdBy;
    private final String creationDate;
    private final Party evaluatedBy;
    private final String evaluatedOn;
    private final String status;
    private final UniqueIdentifier linearId;
    private final List<AbstractParty> partyList;

    public Asset(String assetId, String assetType, int assetValue, Party createdBy, String creationDate, Party evaluatedBy, String evaluatedOn, String status, List<AbstractParty> partyList, UniqueIdentifier linearId) {
        this.assetId = assetId;
        this.assetType = assetType;
        this.assetValue = assetValue;
        this.createdBy = createdBy;
        this.creationDate = creationDate;
        this.evaluatedBy = evaluatedBy;
        this.evaluatedOn = evaluatedOn;
        this.status = status;
        this.partyList = partyList;
        this.linearId = linearId;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getAssetType() {
        return assetType;
    }

    public int getAssetValue() {
        return assetValue;
    }

    public Party getCreatedBy() {
        return createdBy;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public Party getEvaluatedBy() {
        return evaluatedBy;
    }

    public String getEvaluatedOn() {
        return evaluatedOn;
    }

    public String getStatus() {
        return status;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return partyList;
    }
}
