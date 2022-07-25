/**
 * The Assessment Program implements a text-based game which allows the player to guess the
 * Countries and Capitals of the World.
 *
 * @author Jayath Gunawardena
 * @version 1.0
 * @since 23-07-2022
 */

import org.json.JSONArray;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;


public class Assessment {
    public static void main(String[] args) throws IOException, InterruptedException {
        
        init();
    }

    static void init() throws IOException, InterruptedException {

        //Initialisation of variables and objects
        Scanner myObj  = new Scanner(System.in);
        boolean repeat = true;
        int overallHighScore = 0;
        int capitalsHighScore = 0;
        int countryHighScore = 0;
        String[] capitalsArray = new String[250];
        String[] countryArray = new String[250];
        System.out.print("Do you want to play the country listing game? Y/N ");
        String response = myObj.nextLine();

        //Checking if the player wants to play the game and if so plays the game
        if (response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("Y")){
            while(repeat){
                System.out.print("Do you want to play Capitals or Country Names? ");

                int score;
                String mode = myObj.nextLine();
                Date startDate = new Date();
                long startTime = startDate.getTime();
                //Depending on the User Input start the selected game mode.
                if(mode.equalsIgnoreCase("capitals")){
                    mode = "Capitals";
                    //Checks if the user has already played on this particular game mode or not
                    // If they haven't the Array of Countries or Capitals gets populated from the API
                    if(capitalsArray[0] == null){
                        capitalsArray = apiCall(mode);
                    }
                    score = playGame(capitalsArray);
                    if(score > capitalsHighScore) {
                        capitalsHighScore = score;
                    }

                } else{
                    mode = "Countries";
                    if(countryArray[0] == null){
                        countryArray = apiCall(mode);
                    }
                    score = playGame(countryArray);
                    if(score > countryHighScore) {
                        countryHighScore = score;
                    }

                }
                Date endDate = new Date();
                long endTime = endDate.getTime();
                long timeTaken = (endTime - startTime)/1000;

                //Lets the User know of their score for that round.
                System.out.println("Your score for guessing the " + mode + " of the world was " + score + " out of a possible 250");
                System.out.println("You took " +  timeTaken + " seconds");
                if(score > overallHighScore){
                    overallHighScore = score;
                }
                //Gives Users the option to play again depending on their preferences.
                System.out.print("Do you want to play again? Y/N ");
                String playAgain = myObj.nextLine();

                if (!(playAgain.equalsIgnoreCase("yes") || playAgain.equalsIgnoreCase("Y"))) {
                    repeat = false;
                    quitGame("Chose", overallHighScore, capitalsHighScore, countryHighScore);
                }
            }




        }else if(response.equalsIgnoreCase("no") || response.equalsIgnoreCase("N") ){
            quitGame("Start", 0, 0 ,0);
        } else{
            System.out.print("Please enter a valid response, Yes or No ");
            main(null);
        }



    }
    static void quitGame(String ending, int overallHighScore, int capitalsHighScore, int countryHighScore){
        //This method runs when the user wants to quit the game.
        // Depending on when in the process they chose to quit, they will get a different output.
        System.out.println("Thank you for playing the country guessing game");
        if (!ending.equals("Start")) {
            System.out.println("Your overall high score was " + overallHighScore);
            System.out.println("Your highest score for Capitals was " + capitalsHighScore);
            System.out.println("Your highest score for Countries was " + countryHighScore);
        }
    }

    static int playGame(String[] answerList)  {
        //This method runs when the user wants to play the game
        // It takes an array of Countries or Capitals as an input.

        int lives = 3;
        int score = 0;

        String[] guesses = {};

        //Checks if the user still has lives remaining or has already guessed all the countries
        while(lives != 0 && score != 250){
            Scanner guessObj = new Scanner(System.in);
            System.out.print("Enter your guess: ");
            String guess = guessObj.nextLine();

            //Checks if the guess is valid by comparing to all previous guesses
            //As well as checks if the guess is an actual country or Capital.
            //If so score gets iterated and the array of guessed countries increases
            //If not a valid guess the user is told that it's incorrect.
            if(Arrays.stream(guesses).noneMatch(guess::equalsIgnoreCase)){
                if(Arrays.stream(answerList).anyMatch(guess::equalsIgnoreCase)){
                    score ++;
                    System.out.println("Your current score is " + score);
                    String[] tempArr = new String[guesses.length + 1];
                    int i;
                    for(i = 0; i < guesses.length; i++){
                        tempArr[i] = guesses[i];
                    }
                    tempArr[i] = guess;
                    guesses = tempArr;
                } else{

                    System.out.println("Incorrect Guess");
                    lives -= 1;
                    System.out.println("You have " + (lives) + " lives remaining");

                }


            } else {
                //If the user attempts to guess a country they have already guessed they are made aware.
                System.out.println("You have already guessed " + guess + ". Please enter a new guess");
            }




        }
        return score;
    }
    static String[] apiCall(String mode) throws IOException,InterruptedException{
        //This method gets the list of Countries or Capital Names from the API
        // It will run a maximum of two time, one for each of the different modes.
        String[] modeList = new String[250];

        final String GUESSURI = "https://restcountries.com/v3.1/all";

        //The purpose of the try-catch statement is to ensure that no network related errors occur
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GUESSURI))
                    .header("accept", "application/json")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200){
                JSONArray responseBody = new JSONArray(response.body());

                for(int i = 0; i < responseBody.length(); i++) {
                    if(mode.equals("Capitals")) {
                        if (responseBody.getJSONObject(i).has("capital")) {
                            modeList[i] = responseBody.getJSONObject(i).getJSONArray("capital").getString(0);
                        }
                    }else if (mode.equals("Countries")) {
                        modeList[i] = responseBody.getJSONObject(i).getJSONObject("name").getString("common");
                    }

                }
            }else{
                //If there is a networking error, the user will be told
                // As such any networking error is appropriately handled
                System.out.println("A networking error occurred, error code: " + response.statusCode());
                System.exit(0);

            }
        } catch (ConnectException e){
            //If there is a networking error, the user will be told
            // As such any networking error is appropriately handled
            System.out.print("Sorry, please check your network connectivity and try again");
            System.exit(0);


        }

    return modeList;
    }

}
