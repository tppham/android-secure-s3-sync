#!/usr/bin/python
"""
Import this anywhere in your program to get dropped to a debugger when
an exception occurs.
"""

def exceptions(type, value, tb) :
	"""Called for each exception.  Print the location and invoke pdb."""
	import pdb
	import traceback
	traceback.print_exception(type, value, tb)
	print
	pdb.pm()

if __name__ == "__main__" :
	# run from the command line, invoke pdb with cmd line args
	import pdb
	pdb.main()
else :
	# imported from another module.  setup exception hook
	import sys
	sys.excepthook = exceptions

