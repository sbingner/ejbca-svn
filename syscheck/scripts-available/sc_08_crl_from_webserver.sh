#!/bin/bash


# Set default home if not already set.
SYSCHECK_HOME=${SYSCHECK_HOME:-"/usr/local/syscheck"}

# Import common definitions
. $SYSCHECK_HOME/resources.sh

## Local Definitions ##
SCRIPTID=08

getlangfiles $SCRIPTID
getconfig $SCRIPTID

CRL_ERRNO_1=${SCRIPTID}01
CRL_ERRNO_2=${SCRIPTID}02
CRL_ERRNO_3=${SCRIPTID}03
CRL_ERRNO_4=${SCRIPTID}04

# help
if [ "x$1" = "x--help" ] ; then
    echo "$0 $CRL_HELP"
    echo "$CRL_ERRNO_1/$CRL_DESCR_1 - $CRL_HELP_1"
    echo "$CRL_ERRNO_2/$CRL_DESCR_2 - $CRL_HELP_2"
    echo "$CRL_ERRNO_3/$CRL_DESCR_3 - $CRL_HELP_3"
    echo "$CRL_ERRNO_4/$CRL_DESCR_4 - $CRL_HELP_4"
    exit
elif [ "x$1" = "x-s" -o  "x$1" = "x--screen"  ] ; then
    PRINTTOSCREEN=1
fi



checkcrl () {

	CRLNAME=$1
	cd /tmp
	outname=`mktemp`
	wget ${CRLNAME} -T 10 -t 1 -O $outname -o /dev/null
	if [ $? -ne 0 ] ; then
		printlogmess $ERROR $CRL_ERRNO_3 "$CRL_DESCR_3" "$CRLNAME"	
		exit
	fi
	
# file not found where it should be
	if [ ! -f $outname ] ; then
		printlogmess $ERROR $CRL_ERRNO_1 "$CRL_DESCR_1" "$CRLNAME"
		exit 1
	fi

	CRL_FILE_SIZE=`stat -c"%s" $outname`
# stat return check
	if [ $? -ne 0 ] ; then
		printlogmess $ERROR $CRL_ERRNO_4 "$CRL_DESCR_4" "$CRLNAME"	
		exit
	fi

# crl of 0 size?
	if [ "x$CRL_FILE_SIZE" = "x0" ] ; then
		printlogmess $ERROR $CRL_ERRNO_4 "$CRL_DESCR_4" "$CRLNAME"	
		exit
	fi

# now we can check the crl:s best before date is in the future with atleast HOURTHRESHOLD hours (defined in resources)
	TEMPDATE=`openssl crl -inform der -in $outname -lastupdate -noout`
	DATE=${TEMPDATE:11}
	HOURSSINCEGENERATION=`${SYSCHECK_HOME}/lib/cmp_dates.pl "$DATE"`
	
	if [ "$HOURSSINCEGENERATION" -gt "$HOURTHRESHOLD" ] ; then
		printlogmess $ERROR $CRL_ERRNO_1 "$CRL_DESCR_1" "$CRLNAME"
	else
		printlogmess $INFO $CRL_ERRNO_2 "$CRL_DESCR_2" "$CRLNAME"
	fi
	
	# Remove temporary file
	rm "$outname"
}

for (( i = 0 ;  i < ${#CRLS[@]} ; i++ )) ; do
    checkcrl ${CRLS[$i]}
done

