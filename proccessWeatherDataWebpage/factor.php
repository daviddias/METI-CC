<?php
//using preg_match to see if the number is prime
function is_prime($number)
{
        return !preg_match('/^1?$|^(11+?)\1+$/x', str_repeat('1', $number));
}

//lets factor some numbers!
function factor_number($number, $static_num) //we need a static variable "$static_num" to tell you the original number
{
        static $factor = array(); //declare this static so the value won't change everytime the function is called
        if(is_prime($number) === FALSE) //if the number is not prime
        {
                for($i = 2; $i <= $number; $i++) //create our loop to find the first number it is divisible by
                {
                        if($number % $i == 0) //if after division there are no remanders we found our first divisor
                        {
                                $factor[] = $i; //add the current $i number to our factor array so we can display the factored numbers later
                                $number = $number / $i; //divide the number by the divisor
                                
                                if(is_prime($number)) //if the number is now prime we tack on the final number to make the multiplication correct
                                        $factor[] = $number;
                                        
                                factor_number($number, $static_num); //if it's not prime, loop through until the number is prime
                                break;
                        }
                }
        }
        elseif(is_prime($number) === TRUE) //if the number is prime
        {
                echo "Number has been factored.<br/>"; //tell them the number was sucesfully factored
                echo "Factor for {$static_num} ";
                $array_count = count($factor); //count the array values for display
                foreach($factor as $key=>$value)
                {
                        if($key != $array_count - 1) //if the current key of the array is not the last one, display the value with a multiplication sign
                        {
                                echo $value . " X ";
                        }
                        else //else its the last digit and we don't need the sign
                        {
                                echo $value;
                        }
                }
        }
}

factor_number(3557,3557);
/* the function above displays...
Number has been factored.
Factor for 9 is: 3 X 3
*/


function rand_string( $length ) {
	$chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";	
	$size = strlen( $chars );
	for( $i = 0; $i < $length; $i++ ) {
		$str .= $chars[ rand( 0, $size - 1 ) ];
	}

	return $str;
}

for($i = 0; $i < 200; $i++){
	 md5(rand_string(1000));
}

?>
