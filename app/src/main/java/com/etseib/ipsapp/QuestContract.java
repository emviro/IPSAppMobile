package com.etseib.ipsapp;

import android.provider.BaseColumns;

public class QuestContract {

    private QuestContract(){} //to avoid creating an object of this class

    /*public static class QuestionsTable implements BaseColumns {
        public static final String TABLE_NAME = "Questions";
        public static final String COL_QUESTION = "Questions";
        public static final String COL_OPTION_A = "OptionA";
        public static final String COL_OPTION_B = "OptionB";
        public static final String COL_OPTION_C = "OptionC";
    }*/
    public static class UsersTable implements BaseColumns{
        public final static String TABLE_NAME = "users";
        public static final String COL_EMAIL = "Email";
        public final static String COL_NAME = "Name";
        public static final String COL_SEX = "Sex";
        public static final String COL_CHANGE_PASSWORD = "ChangePassword"; //1 (true) if password needs to be changed, 0 (false) otherwise
        public static final String COL_STATUS = "Status";
        public static final String COL_LAST_COMPLETED_QUESTION= "LastCompletedQuestion";
        public static final String COL_IN_GAME = "InGame";
        public static final String COL_CURRENT_CONCEPT = "CurrentConcept";
        public static final String COL_EB = "EB";
        public static final String COL_PB = "PB";
        public static final String COL_SEEN_IMAGES = "SeenImages";
        public static final String COL_CURRENT_IMAGE = "CurrentImage";
        public static final String COL_SEEN_PIECES = "SeenPieces";
    }
    public static class AnswersTable implements BaseColumns{
        public final static String TABLE_NAME = "answers";
        public final static String COL_EMAIL = "Email";
        public final static String COL_QUESTION = "Question";
        public final static String COL_ANSWER = "Answer";
        public final static String COL_CAUSE = "Cause";
        public static final String COL_PERCENTAGE = "Percentage";
    }

    public static class ConceptsTable implements BaseColumns{
        public final static String TABLE_NAME = "concepts";
        public final static String COL_ID_CONCEPT = "IdConcept";
        public final static String COL_CONCEPT = "Concept";

    }

    public static class ConceptImagesTable implements BaseColumns{
        public final static String TABLE_NAME = "conceptImages";
        public final static String COL_ID_CONCEPT_IMAGE = "IdConceptImage";
        public final static String COL_ID_CONCEPT = "ConceptId";
        public final static String COL_IMAGE_NAME = "ImageName";

    }

}
