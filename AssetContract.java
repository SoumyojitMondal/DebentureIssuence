package net.corda.samples.contracts;

import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.samples.states.Asset;
import org.jetbrains.annotations.NotNull;

public class AssetContract implements Contract {

    public final static String ID = "net.corda.samples.contracts.AssetContract";
    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        if(tx.getCommands().size() == 0){
            throw new IllegalArgumentException("One command Expected");
        }
        Command command = tx.getCommand(0);
        if(command.getValue() instanceof AssetContract.Commands.CreateAsset)
            verifyAssetCreation(tx);
        else if(command.getValue() instanceof AssetContract.Commands.UpdateStatus)
            verifyStatusUpdate(tx);
        else
            throw new IllegalArgumentException("Invalid command");
    }

    private void verifyAssetCreation(LedgerTransaction tx){
        if(tx.getInputStates().size() != 0) throw new IllegalArgumentException("Zero Input state Expected");
        if(tx.getOutputStates().size() != 1) throw new IllegalArgumentException("One Output Expected");

        Asset outputState = (Asset) tx.getOutput(0);
        CommandWithParties command = tx.getCommands().get(0);
        if(outputState.getAssetType().length() < 3)
            throw new IllegalArgumentException("Asset type must be more than 3 characters");
        if(outputState.getAssetValue() < 100000)
            throw new IllegalArgumentException("Asset value must be greater than INR 100000");
        if(outputState.getCreatedBy() == null){
            throw new IllegalArgumentException("CreatedBy can't be left blank");
        }
    }

    private void verifyStatusUpdate(LedgerTransaction tx){
        if(tx.getInputStates().size() != 0) throw new IllegalArgumentException("Zero Input state Expected");
        if(tx.getOutputStates().size() != 1) throw new IllegalArgumentException("One Output Expected");

        Asset outputState = (Asset) tx.getOutput(0);
        CommandWithParties command = tx.getCommands().get(0);
        if(outputState.getEvaluatedBy() == null){
            throw new IllegalArgumentException("EvaluatedBy can't be left blank");
        }
        if(outputState.getEvaluatedOn() == null){
            throw new IllegalArgumentException("EvaluatedOn can't be left blank");
        }
        if(outputState.getStatus() == null){
            throw new IllegalArgumentException("Status can't be left blank");
        }
    }


    public interface Commands extends CommandData {
        class CreateAsset implements Commands {}
        class UpdateStatus implements Commands {}
    }
}
