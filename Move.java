import java.io.Serializable;

/**
     * A move object containing the from position and the to position
     * Checks to see if the move created is a jump move by seeing if the move is +2 rows or -2 rows
     */
    public class Move implements Serializable{
        /**
    *
    */
        private static final long serialVersionUID = 7536754792343372098L;
        int fR, fC;
        int tR, tC;

        public Move(int fR, int fC, int tR, int tC) {
            this.fR = fR;
            this.fC = fC;
            this.tR = tR;
            this.tC = tC;
        }   
        public boolean isJump() {
            return (fR - tR == 2 || fR - tR == -2);
        }
        public String toString() {
            return "Move from r" + fR +", c"+fC + " to r" + tR +", c"+tC;
        }
    }