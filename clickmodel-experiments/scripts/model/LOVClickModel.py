#!/usr/bin/python

import time
import sys
from ClickModelExperiment import ClickModelExperiment

__author__ = 'Anonymous'


if __name__ == "__main__":

    print 'Argument List:', str(sys.argv)
    print len(sys.argv)
    if (len(sys.argv) < 5):
        print 'Usage: python LOVClickModel.py resource_path output_path search_log_file click_model_names...'

    resource_path = sys.argv[1]
    output_path = sys.argv[2]
    search_log_file = sys.argv[3]
    click_model_names = sys.argv[4:len(sys.argv)]

    print resource_path
    print output_path
    print search_log_file
    print click_model_names


    search_sessions_num = None
    # search_sessions_num = 100

    for click_model_name in click_model_names:
        print "Runing: " + click_model_name
        click_model_experiment = ClickModelExperiment(output_path, search_log_file, click_model_name, search_sessions_num)
        click_model_experiment.run_experiment()

