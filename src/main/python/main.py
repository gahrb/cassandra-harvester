import json
import sys
import logging

FORMAT = '%(asctime)-5s [%(name)s] %(levelname)s: %(message)s'
logging.basicConfig(filename='/var/log/kraken/kraken_log.log',level=logging.INFO,format=FORMAT)
logger = logging.getLogger('kraken_crawler')


def main(argv):
    print "let's start here"







if __name__ == "__main__":
    main(sys.argv[1:])
