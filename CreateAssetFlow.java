package net.corda.samples.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.StatesToRecord;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.samples.contracts.AssetContract;
import net.corda.samples.states.Asset;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * This flow is used to build a transaction to issue an asset on the Corda Ledger, which can later be put on auction.
 * It creates a self issues transaction, the state is only issued on the ledger of the party who executes the flow.
 */
@InitiatingFlow
@StartableByRPC
public class CreateAssetFlow extends FlowLogic<SignedTransaction> {

    private String assetType;
    private int assetValue;
    //private Party evaluatedBy;
    public CreateAssetFlow(String assetType, int assetValue) {
        this.assetType = assetType;
        this.assetValue = assetValue;
        //this.evaluatedBy = evaluatedBy;
    }


    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {

        Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        UniqueIdentifier referenceUUID = new UniqueIdentifier();

        Party createdBy = getOurIdentity();
        Timestamp instant= Timestamp.from(Instant.now());
        String creationDate = instant.toString();
        String status = "Proposed";
        String evaluatedOn = "";
        Party evaluatedBy = null;
        List<AbstractParty> partyList = new ArrayList<AbstractParty>();
        partyList.add(getOurIdentity());

        if(!getOurIdentity().getName().getOrganisation().contains("Issuer")){
            throw new FlowException("Only Issuer is allowed to propose asset for debenture issuance");
        }
        List<Party> participantsList = getServiceHub().getNetworkMapCache().getAllNodes().stream()
                .map(nodeInfo -> nodeInfo.getLegalIdentities().get(0))
                .collect(Collectors.toList());
        participantsList.remove(createdBy);
        //participantsList.remove(evaluatedBy);
        participantsList.remove(notary);

        // Create the output state
        Asset output = new Asset(referenceUUID.toString(), assetType, assetValue, createdBy, creationDate, evaluatedBy, evaluatedOn, status, partyList, referenceUUID);

        // Build the transaction, add the output state and the command to the transaction.

        TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                .addOutputState(output)
                .addCommand(new AssetContract.Commands.CreateAsset(),getOurIdentity().getOwningKey());

        // Verify the transaction
        transactionBuilder.verify(getServiceHub());

        // Sign the transaction
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);

       // List<FlowSession> participantsSessions = new ArrayList<>();

        //FlowSession counterPartySession = initiateFlow(evaluatedBy);
        //counterPartySession.send(false);
        subFlow(new FinalityFlow(signedTransaction, Collections.emptyList()));
        for(Party participant: participantsList){
            if(!participant.getName().toString().contains("Investor")){
                FlowSession session = initiateFlow(participant);
                session.send(true);
                subFlow(new SendTransactionFlow(session, signedTransaction));
            }
        }
        return signedTransaction;
    }

    @InitiatedBy(CreateAssetFlow.class)
    public static class Responder extends FlowLogic<SignedTransaction> {

        private FlowSession counterpartySession;

        public Responder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            boolean output = counterpartySession.receive(Boolean.class).unwrap(it -> it);
            if(!output)
                return subFlow(new ReceiveFinalityFlow(counterpartySession));

            else
                return subFlow(new ReceiveTransactionFlow(counterpartySession, true, StatesToRecord.ALL_VISIBLE));

        }
    }
}