__author__ = 'Anonymous'

import time
import csv
import os.path
import pyclick

from pyclick.utils.YandexRelPredChallengeParser import YandexRelPredChallengeParser
from pyclick.utils.Utils import Utils
from pyclick.click_models.Evaluation import LogLikelihood, Perplexity
from pyclick.click_models.UBM import UBM
from pyclick.click_models.DBN import DBN
from pyclick.click_models.SDBN import SDBN
from pyclick.click_models.DCM import DCM
from pyclick.click_models.CCM import CCM
from pyclick.click_models.CTR import DCTR, RCTR, GCTR
from pyclick.click_models.CM import CM
from pyclick.click_models.PBM import PBM

#
# Based on the original PyClick example from Ilya Markov: https://github.com/markovi/PyClick/blob/master/examples/Example.py
#
class ClickModelExperiment:

    def __init__(self, output_path, search_log_file, click_model_name, search_sessions_num):
        self.output_path = output_path
        self.search_log_file = search_log_file
        self.click_model_name = click_model_name
        self.search_sessions_num = search_sessions_num
        self.click_props_filename = "ClickProbability"
        self.satisfaction_probs_filename = "SatisfactionProbability"
        self.model_performance_filename = "PerformanceResults"
        self.model_path = self.output_path + "models/"
        try:
            os.makedirs(self.model_path)
        except OSError:
            print ("folder exists")
        else:
            print ("folder created")


    def run_experiment(self):
        click_model = globals()[self.click_model_name]()

        search_sessions = YandexRelPredChallengeParser().parse(self.search_log_file, self.search_sessions_num)

        train_test_split = int(len(search_sessions) * 0.75)
        train_sessions = search_sessions[:train_test_split]
        train_queries = Utils.get_unique_queries(train_sessions)

        test_sessions = Utils.filter_sessions(search_sessions[train_test_split:], train_queries)
        test_queries = Utils.get_unique_queries(test_sessions)

        print "-------------------------------"
        print "Training on %d search sessions (%d unique queries)." % (len(train_sessions), len(train_queries))
        print "-------------------------------"

        start = time.time()
        click_model.train(train_sessions)
        end = time.time()
        print "\tTrained %s click model in %i secs:\n%r" % (click_model.__class__.__name__, end - start, click_model)

        self.evaluate_click_model(click_model, train_sessions, train_queries, test_sessions, test_queries)

        model_file = self.model_path + click_model.__class__.__name__ + ".json"
        with open(model_file, mode='w') as model_file:
            model_file.write(click_model.to_json())

        self.get_click_probs(click_model, search_sessions)
        self.get_satisfaction_probs(click_model, search_sessions)


    def evaluate_click_model(self, click_model, train_sessions, train_queries, test_sessions, test_queries):

        print "-------------------------------"
        print "Testing on %d search sessions (%d unique queries)." % (len(test_sessions), len(test_queries))
        print "-------------------------------"

        loglikelihood = LogLikelihood()
        perplexity = Perplexity()

        start = time.time()
        ll_value_train = loglikelihood.evaluate(click_model, train_sessions)
        ll_value_test = loglikelihood.evaluate(click_model, test_sessions)
        end = time.time()
        print "\tlog-likelihood: %f; time: %i secs" % (ll_value_test, end - start)

        start = time.time()
        perp_value_train = perplexity.evaluate(click_model, train_sessions)[0]
        perp_value_test = perplexity.evaluate(click_model, test_sessions)[0]
        end = time.time()
        print "\tperplexity: %f; time: %i secs" % (perp_value_test, end - start)

        model_performance_path = self.output_path + self.model_performance_filename + ".csv"

        if not os.path.isfile(model_performance_path):
            print "file does not exist"
            with open(model_performance_path, mode='w') as model_performance_file:
                performance_writer = csv.writer(model_performance_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_ALL)
                performance_writer.writerow(['TimeStamp', 'ClickLogFile', 'ClickModel', 'SearchSessions_Train', 'UniqueQueries_Train', 'SearchSessions_Test', 'UniqueQueries_Test', 'LogLikelihood_Train', 'LogLikelihood_Test',  'Perplexity_Train', 'Perplexity_Test'])

        with open(model_performance_path, mode='a') as model_performance_file:
            performance_writer = csv.writer(model_performance_file, delimiter=',', quotechar='"', quoting=csv.QUOTE_ALL)
            performance_writer.writerow([time.time(), self.search_log_file, click_model.__class__.__name__, len(train_sessions), len(train_queries), len(test_sessions), len(test_queries), ll_value_train, ll_value_test, perp_value_train, perp_value_test])


    def get_click_probs(self, click_model, search_sessions):
        click_probabilites = []
        query_cache = []
        for search_session in search_sessions:
            query = search_session.query
            if query not in query_cache:
                query_cache.append(query)
                web_results = search_session.web_results
                click_probs = click_model.get_full_click_probs(search_session)
                for x in range(len(web_results)):
                    web_result = web_results[x]
                    click_prob = str(click_probs[x])
                    click_probabilites.append("\"" + query + "\",\"" + web_result.id + "\",\"" + click_prob + "\"\n")
                # for rank, click_prob in enumerate(click_probs):
                #     print str(rank) + " " + str(click_prob)

        # '../resources/output/VocabRankingClickProbabilities-v2.txt'
        click_props_path = self.output_path + click_model.__class__.__name__ + "_" + self.click_props_filename +  "_Raw.csv"
        with open(click_props_path, 'w') as out:
            out.writelines(click_probabilites)
            out.close()

    def get_satisfaction_probs(self, click_model, search_sessions):
        satisfaction_probs = []
        query_cache = []
        for search_session in search_sessions:
            query = search_session.query
            if query not in query_cache:
                query_cache.append(query)
                web_results = search_session.web_results
                for x in range(len(web_results)):
                    web_result = web_results[x]
                    satisfaction = click_model.predict_relevance(query, web_result.id)
                    # print query + " - " + web_result.id + " - " + str(relevance)
                    satisfaction_probs.append("\"" + query + "\",\"" + web_result.id + "\",\"" + str(satisfaction) + "\"\n")
        satisfaction_probs_path = self.output_path + click_model.__class__.__name__ +  "_" + self.satisfaction_probs_filename + "_Raw.csv"
        with open(satisfaction_probs_path, 'w') as out:
            out.writelines(satisfaction_probs)
            out.close()

