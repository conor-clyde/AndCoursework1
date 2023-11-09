package com.example.dicefrenzy;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Objects;

public class DisplayGame extends AppCompatActivity {
    // Variable declarations
    private static final int MIN = 1;
    private static final int MAX = 6;
    private static final int ROLL_TYPE_PLAYER = 1;
    private static final int ROLL_TYPE_COM = 2;

    private final String[] fileName = {"die_face_1", "die_face_2", "die_face_3",
            "die_face_4", "die_face_5", "die_face_6"};
    private final String[] fileNameSel = {"die_face_1_sel", "die_face_2_sel", "die_face_3_sel",
            "die_face_4_sel", "die_face_5_sel", "die_face_6_sel"};

    private final int[] rollOptions = {1, 1, 1, 1, 1};
    private final int[] rollOptionsCom = {1, 1, 1, 1, 1};
    private final int[] dieScores = {0, 0, 0, 0, 0};
    private final int[] dieScoresCom = {0, 0, 0, 0, 0};

    private static int playerWins = 0;
    private static int comWins = 0;

    private int diceScore = 0;
    private int diceScoreCom = 0;
    private int totalScore = 0;
    private int totalScoreCom = 0;
    private int noRolls = 0;
    private int goal = 0;
    private final int noDice = 5;

    private boolean tie = false;

    private TextView txtScore;
    private TextView txtScoreCom;
    private TextView txtInstruct;
    private TextView txtGoal;
    private TextView txtDice;
    private TextView txtDiceCom;
    private TextView txtWins;

    private View vwHorizontal;
    private View vwVertical;

    private EditText edtGoal;

    private ImageView[] playerDie;
    private ImageView[] comDie;

    private Button btnThrow;
    private Button btnScore;
    private Button btnGoal;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_game);

        initializeViews();
        setClickListeners();

        // Display total player and computer wins
        txtWins.setText("H:" + playerWins + "/C:" + comWins);
    }

    private void initializeViews()
    {
        // Initialize views used in the game page, i.e. assign variable created earlier to the correct view id
        txtScore = findViewById(R.id.txtPlayerScore);
        txtScoreCom = findViewById(R.id.txtComScore);
        txtInstruct = findViewById(R.id.txtInstruct);
        txtGoal = findViewById(R.id.txtGoal);
        txtWins = findViewById(R.id.txtWins);
        txtDice = findViewById(R.id.txtPlayerDice);
        txtDiceCom = findViewById(R.id.txtComDice);

        edtGoal = findViewById(R.id.editGoal);

        btnThrow = findViewById(R.id.btnThrow);
        btnScore = findViewById(R.id.btnScore);
        btnGoal = findViewById(R.id.btnGoal);

        vwHorizontal = findViewById(R.id.vwHorizontal);
        vwVertical = findViewById(R.id.vwVertical);

        // Create arrays for containing all player and computer dice image views
        playerDie = new ImageView[5];
        comDie = new ImageView[5];

        playerDie[0] = findViewById(R.id.imgPlayerDie1);
        playerDie[1] = findViewById(R.id.imgPlayerDie2);
        playerDie[2] = findViewById(R.id.imgPlayerDie3);
        playerDie[3] = findViewById(R.id.imgPlayerDie4);
        playerDie[4] = findViewById(R.id.imgPlayerDie5);

        comDie[0] = findViewById(R.id.imgComDie1);
        comDie[1] = findViewById(R.id.imgComDie2);
        comDie[2] = findViewById(R.id.imgComDie3);
        comDie[3] = findViewById(R.id.imgComDie4);
        comDie[4] = findViewById(R.id.imgComDie5);
    }

    private void setClickListeners()
    {
        btnGoal.setOnClickListener(view -> {
            hideKeyboard();
            startMatch();
        });

        btnThrow.setOnClickListener(view -> handleThrow());

        btnScore.setOnClickListener(view -> handleScore());

        // Handle user click for each player die
        setDieClickListener(R.id.imgPlayerDie1, 0);
        setDieClickListener(R.id.imgPlayerDie2, 1);
        setDieClickListener(R.id.imgPlayerDie3, 2);
        setDieClickListener(R.id.imgPlayerDie4, 3);
        setDieClickListener(R.id.imgPlayerDie5, 4);
    }

    private void setDieClickListener(int id, final int index) {
        ImageView die = findViewById(id);
        die.setOnClickListener(view -> toggleRollOption(index));
    }

    private void toggleRollOption(int i) {
        // Only act on a player die click if it is not the player's first roll
        if (noRolls != 0) {
            // Set the die that the player clicks as selected (change the background to indicate this)
            // However if a selected die is being clicked, revert it to unselected (remove the background which indicates selected)
            if (rollOptions[i] == 1) {
                rollOptions[i] = 0;

                String fileStr = fileNameSel[dieScores[i] - 1];

                int resourceId = this.getResources().getIdentifier(fileStr, "drawable", this.getPackageName());
                playerDie[i].setImageResource(resourceId);
            } else {
                rollOptions[i] = 1;

                setDieImage(dieScores[i], i, playerDie);
            }
        }
    }

    private void hideKeyboard()
    {
        // Remove keyboard from the screen after user inputs a goal
        // Catch block prevents app crashing by skipping the code to remove keyboard if it is not present
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        } catch (Exception ignored) {
        }
    }

    private void handleThrow()
    {
        // Roll the player's dice
        rollDice(ROLL_TYPE_PLAYER);

        // If game is in a "tie" state, perform different actions
        // Computer dice is rolled along with player roll then the score is checked to check if either has won the game yet
        if (tie) {
            rollDice(ROLL_TYPE_COM);
            handleScore();
        }
        // If game is not in a "tie" state
        else {
            // Increase the player's roll per turn count
            noRolls++;

            // Display score button so player can score their throw
            btnScore.setVisibility(VISIBLE);

            // Different instructions provided and actions depending the number of player rolls
            switch (noRolls) {
                case 1:
                    // If it is the player's first throw: Computer dice is also rolled
                    txtInstruct.setText("Click Score to score your throw.\nOr select dice to keep and throw again!\n(2 rolls left)");
                    rollDice(ROLL_TYPE_COM);
                    break;
                case 2:
                    // If it is the player's second throw: Computer dice is not rolled
                    txtInstruct.setText("Click Score to score your throw.\nOr select dice to keep and throw again!\n(1 roll left).");
                    break;
                case 3:
                    // If it is the player's third throw: The round ends and scoring is handled
                    handleScore();
            }
        }
    }

    private void handleScore()
    {
        if (tie)
            // If game is in tie state, remove score button as player can only throw once in a turn
            btnScore.setVisibility(INVISIBLE);
        else
            // If game is not in a tie state
            // Computer decides whether it would like to use its two optional extra rolls
            comDecision();

        // Dice totals are checked
        getDiceTotal();

        // After totaling, the game checks if the goal has been reached by either player, computer, or both
        checkGoalReached();

        // Reset the round so that the player can continue to play until goal is reached or an ongoing tie is settled
        resetRound();
    }

    private void startMatch() {
        // Handle issues with user input: If input is in the incorrect format (text instead of numbers) or goal is too large
        // If no issues, prepare a new match for the player
        try {
            if (!validInt(edtGoal.getText().toString()))
                txtInstruct.setText("Set the match goal to start!\nError: Please enter a numerical goal.");
            else if (!validIntLimit(Integer.parseInt(edtGoal.getText().toString())))
                txtInstruct.setText("Set the match goal to start!\nError: Goal must not exceed 1000 or be a negative value");
            else {
                // Assign user inputted goal to the goal variable to be used in the game
                goal = Integer.parseInt(edtGoal.getText().toString());

                // Remove goal input elements as they are no longer required
                btnGoal.setVisibility(INVISIBLE);
                edtGoal.setVisibility(INVISIBLE);

                // Display game information and UI  so that the player can play the game
                btnThrow.setVisibility(VISIBLE);
                txtGoal.setText("Goal: " + goal);
                txtInstruct.setText("Match started.\nClick throw to roll your dice!\n(3 rolls left)");
                txtGoal.setVisibility(VISIBLE);
                txtScore.setVisibility(VISIBLE);
                txtScoreCom.setVisibility(VISIBLE);
                txtDice.setVisibility(VISIBLE);
                txtDiceCom.setVisibility(VISIBLE);
                vwHorizontal.setVisibility(VISIBLE);
                vwVertical.setVisibility(VISIBLE);

                // If this is not the first match, reset game variables so that the player can play the new match
                if (playerWins != 0 && comWins != 0)
                    resetMatch();
            }
        }
        catch (NumberFormatException e)
        {
            txtInstruct.setText("Set the match goal to start!\nError: Goal must not exceed 1000 or be a negative value");
        }
    }

    private boolean validInt(String text)
    {
        // Check if every character in the user's input is an integer. Return true if they are, otherwise return false.
        boolean valid = true;

        for (int a=0; a < text.length(); a++) {
            if (a == 0 && text.charAt(a) == '-')
                continue;
            if (!Character.isDigit(text.charAt(a)))
                valid = false;
        }

        return valid;
    }

    private boolean validIntLimit(int num)
    {
        // Check if number is between the max and min. Return true if it is, otherwise return false
        return num <= 1000 && num >= 0;
    }

    private void resetMatch() {
        Arrays.fill(dieScores, 0);
        Arrays.fill(dieScoresCom, 0);
        Arrays.fill(rollOptions, 1);
        Arrays.fill(rollOptionsCom, 1);

        tie = false;
        diceScore = 0;
        diceScoreCom = 0;
        totalScore = 0;
        totalScoreCom = 0;

        // Set current match score display to zero
        txtScore.setText("Score: 0");
        txtScoreCom.setText("Com Score: 0");
    }

    private void resetRound()
    {
        if (!tie)
            // If a tie is not ongoing, display the previous round scoring and current instructions
            txtInstruct.setText("You Scored " + diceScore + "!\nOpponent scored " + diceScoreCom + ".\nClick throw to roll (3 rolls left)");

        // Display player and computer scores to the player
        txtScore.setText("Score: " + totalScore);
        txtScoreCom.setText("Com Score: " + totalScoreCom);

        // Hide the score button as user can only throw at the start of a round
        btnScore.setVisibility(INVISIBLE);

        // Remove the black border from each die that was selected to not be rerolled in the prior round
        for (int i = 0; i < noDice; i++) {
            if (rollOptions[i] == 0)
                setDieImage(dieScores[i], i, playerDie);
        }

        // Reset the round's variables so that they can be used in the next round
        noRolls = 0;
        diceScore = 0;
        diceScoreCom = 0;

        Arrays.fill(rollOptions, 1);
        Arrays.fill(rollOptionsCom, 1);
    }

    private void getDiceTotal()
    {
        // For each player die, add the die's value to the total dice score for the round
        for (int dieScore : dieScores)
            diceScore += dieScore;
        // For each computer die, add the die's value to the total dice score for the round
        for (int dieScoreCom : dieScoresCom)
            diceScoreCom += dieScoreCom;

        // Add round total to the match total for the player and computer
        totalScore += diceScore;
        totalScoreCom += diceScoreCom;
    }

    private void checkGoalReached()
    {
        // Check if the player or computer total score is equal to or greater than the goal
        if (totalScore >= goal || totalScoreCom >= goal) {
            // Check if the player and computer have the same total score, i.e. they tied
            if (totalScore == totalScoreCom) {
                txtInstruct.setText("Match tied!\nThrow again to determine a winner");
                // Set tie equal to true so that the tie state of the game can be handled in the next throw
                tie = true;
            }
            // Check if the player's total score is greater than the computer's total score, i.e. the player won
            else if (totalScore > totalScoreCom) {
                // Increase the total number of player wins to be displayed at the top of the next game
                playerWins++;

                // Handle the won status of the match
                showResults("win");
            }
            // Check if the computer's total score is greater than the player's total score, i.e. the player lost
            else {
                // Increase the total number of computer wins to be displayed at the top of the next game
                comWins++;

                // Handle the lost status of the match
                showResults("lose");
            }
        }
    }

    public void rollDice(int type) {
        // Initialise an array of Integers for the required amount of dice per player and computer
        int[] randVal = new int[noDice];

        // For each player and computer die
        for (int i = 0; i < noDice; i++) {
            // Check if player/computer wants to roll the current dice and only roll it if they do
            if ((type == ROLL_TYPE_PLAYER && rollOptions[i] == 1) || (type == ROLL_TYPE_COM && rollOptionsCom[i] == 1)) {
                // Get a random value between MIN (1) nad MAX (6)
                randVal[i] = randomNumbers();

                // If player roll, add random value to player die score array
                if (type == ROLL_TYPE_PLAYER)
                    dieScores[i] = randVal[i];
                // If computer roll, add random value to computer die score array
                else dieScoresCom[i] = randVal[i];

                // If player roll, set the correct image for the value scored to their die
                if (type == ROLL_TYPE_PLAYER)
                    setDieImage(randVal[i], i, playerDie);
                // If computer roll, set the correct image for the value scored to their die
                else setDieImage(randVal[i], i, comDie);
            }
        }
    }

    private void setDieImage(int randVal, int i, ImageView[] die)
    {
        String fileStr = fileName[randVal - 1];

        int resourceId = this.getResources().getIdentifier(fileStr, "drawable", this.getPackageName());
        die[i].setImageResource(resourceId);
    }

    public void showResults(String status) {
        // Initialise an AlertDialog which will display the player's match results
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        alertDialog = dialogBuilder.create();

        // Set up a button on the alertDialog which will allow the player to return to the game menu as the current match is finished
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Return to Game Menu", (dialog, which) -> navigateUpTo(new Intent(getBaseContext(), MainActivity.class)));

        // Do not allow player to click out of alertDialog. They can only click the return to game menu button
        alertDialog.setCancelable(false);

        // If the player won
        if (tie)
            // If a tied was settled, describe the tie winner
            alertDialog.setMessage("Tie settled!\nYou scored " + diceScore + " (Total: " + totalScore + ")\nOpponent scored " + diceScoreCom + " (Total: " + totalScoreCom + ")");
        else
            // Otherwise show final scores
            alertDialog.setMessage("Your score: " + totalScore + "\nOpponent's score: " + totalScoreCom);

        // Handle either a player win or a player lose
        if(alertDialog != null && !alertDialog.isShowing()) {
            switch (status) {
                case "win":
                    // If the player won
                    // Set the alertDialog title to "You Won"
                    alertDialog.setTitle("You won!");

                    // Display the alertDialog
                    alertDialog.show();

                    // Set the alertDialog title text colour to green
                    setResultsColour("#02C984");
                    break;
                case "lose":
                    // If the player lost
                    // Set the alertDialog title to "You Lost"
                    alertDialog.setTitle("You Lost!");

                    // Display the alertDialog
                    alertDialog.show();

                    // Set the alertDialog title text colour to red
                    setResultsColour("#B01E3A");
            }
        }
    }

    private void setResultsColour(String color)
    {
        // Get the alert title id and store it in a variable
        int textViewId = alertDialog.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);

        // If variable is not empty
        if (textViewId != 0) {
            // Initialise alert title as textview and set its text colour property
            TextView tv = alertDialog.findViewById(textViewId);
            tv.setTextColor(Color.parseColor(color));
        }
    }

    /* Computer Strategy:
    The following block comment provides insight into the computer's decision-making strategies for re-rolling in the game.
    The computer employs these three strategies for its two re-roll opportunities available during each turn.

    Strategy 1:
    The potential scores range from 5 to 35.
    The primary objective of the computer in this strategy is to avoid the lowest possible score combinations for a round.
    To accomplish this, scores are categorized into three tiers: 5-15 (lowest third), 16-25 (middle third), and 26-35 (top third).
    If the computer's initial roll places it in the lowest third, it considers the roll unfavorable and triggers a re-roll.
    During re-rolls, the computer retains dice that have scored 4 points or more and re-rolls those that scored 3 points or less.
    The objective is to transition out of the lowest third of possible round score combinations.
    If the computer remains within the lowest third after the initial re-roll, it proceeds to take a second re-roll.
    ------
    Strategy 2:
    When the computer is losing in the match (i.e., the player's total score exceeds the computer's total score), it adopts a more aggressive approach.
    Even if the computer's initial roll does not fall in the lowest third,
    it will initiate a re-roll if it does not place it in the upper half of possible round score combinations.
    The objective is to elevate its score to the top half to narrow the gap with the player or potentially surpass them.
    Similar to Strategy 1, the computer retains dice that have scored 4 points or more and re-rolls those that scored 3 points or less.
    If it remains within the bottom half of scores (or lowest third of scores), it proceeds to take a second re-roll.
    ------
    Strategy 3:
    In scenarios where the computer has one of its two re-rolls available and has not employed either of the first two strategies,
    it will perform a re-roll on dice with a value of one or two.
    The objective is to increase the values of these dice, reducing the prevalence of low-value dice in the computer's turn.
    If the computer still has ones or twos left (or is positioned within the bottom half of scores and is losing, or is within the lowest third of scores),
    it proceeds to take a second re-roll to increase the value of the one and two-valued dice
    (or increase its score if within the bottom half or lowest third of scores).
    */

    private void comDecision()
    {
        boolean comRoll = false;
        int diceScoreComDec = 0;

        // Add up computer's current round score to assist its decision making
        for (int dieScoreCom : dieScoresCom)
            diceScoreComDec += dieScoreCom;

        // For each of the computer's two re-roll options
        for (int i=0; i < 2; i++) {
            // If on second loop and computer did not use their first re-roll, skip over the second re-roll decision

            // If computer's current round score is under 16, re-roll every die that is lower than four
            if (diceScoreComDec < 16) {
                comRoll = true;

                for (int j = 0; j < noDice; j++) {
                    if (dieScoresCom[j] >= 4)
                        rollOptionsCom[j] = 0;
                }
            }

            // If computer is losing and their current round score is under 21, re-roll every die that is lower than four
            if (!comRoll && (totalScoreCom < totalScore) && diceScoreComDec < 21) {
                comRoll = true;

                for (int j = 0; j < noDice; j++) {
                    if (dieScoresCom[j] >= 4)
                        rollOptionsCom[j] = 0;
                }
            }

            // If the computer has not decided to re-roll yet, re-roll every one and two valued dice
            if (!comRoll) {
                comRoll = true;

                for (int j = 0; j < noDice; j++) {
                    if (dieScoresCom[j] >= 3)
                        rollOptionsCom[j] = 0;
                }
            }

            // Perform the re-roll if the computer decided to perform one
            rollDice(ROLL_TYPE_COM);
        }
    }

    private int randomNumbers() {
        int range = DisplayGame.MAX - DisplayGame.MIN + 1;

        // Generate a random number within min and max
        return (int) (Math.random() * range) + DisplayGame.MIN;
    }
}