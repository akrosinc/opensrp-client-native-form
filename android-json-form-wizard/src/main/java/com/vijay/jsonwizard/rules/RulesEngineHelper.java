package com.vijay.jsonwizard.rules;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.RuleListener;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.jeasy.rules.core.RulesEngineParameters;
import org.jeasy.rules.mvel.MVELRuleFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RulesEngineHelper implements RuleListener {
    public final String TAG = RulesEngineHelper.class.getCanonicalName();
    private Context context;
    private RulesEngine defaultRulesEngine;
    private Map<String, Rules> ruleMap;
    private final String RULE_FOLDER_PATH = "rule/";
    private Rules rules;
    private String selectedRuleName;
    private Gson gson;
    private Map<String, String> globalValues;

    public RulesEngineHelper(Context context, Map<String, String> globalValues) {
        this.context = context;
        RulesEngineParameters parameters = new RulesEngineParameters().skipOnFirstAppliedRule(true);
        this.defaultRulesEngine = new DefaultRulesEngine(parameters);
        ((DefaultRulesEngine) this.defaultRulesEngine).registerRuleListener(this);
        this.ruleMap = new HashMap<>();
        gson = new Gson();
        this.globalValues = globalValues;

    }

    private Rules getRulesFromAsset(String fileName) {
        try {
            if (!ruleMap.containsKey(fileName)) {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open(fileName)));
                ruleMap.put(fileName, MVELRuleFactory.createRulesFrom(bufferedReader));
            }
            return ruleMap.get(fileName);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    protected void processDefaultRules(Rules rules, Facts facts) {

        defaultRulesEngine.fire(rules, facts);
    }

    public boolean getRelevance(Map<String, String> relevanceFact, String ruleFilename) {

        Facts facts = initializeFacts(relevanceFact);

        facts.put(RuleConstant.IS_RELEVANT, false);

        rules = getRulesFromAsset(RULE_FOLDER_PATH + ruleFilename);

        processDefaultRules(rules, facts);

        return facts.get(RuleConstant.IS_RELEVANT);
    }

    public String getCalculation(Map<String, String> calculationFact, String ruleFilename) {

        Facts facts = initializeFacts(calculationFact);

        facts.put(RuleConstant.CALCULATION, "0");

        rules = getRulesFromAsset(RULE_FOLDER_PATH + ruleFilename);

        processDefaultRules(rules, facts);

        return String.valueOf(facts.get(RuleConstant.CALCULATION));
    }

    private Facts initializeFacts(Map<String, String> factMap) {

        if (globalValues != null) {
            factMap.putAll(globalValues);
        }

        selectedRuleName = factMap.get(RuleConstant.SELECTED_RULE);

        Facts facts = new Facts();

        for (Map.Entry<String, String> entry : factMap.entrySet()) {


            facts.put(getKey(entry.getKey()), isList(entry.getValue()) ? gson.fromJson(entry.getValue(), ArrayList.class) : entry.getValue());
        }

        return facts;
    }

    private String getKey(String key) {
        return !key.startsWith(RuleConstant.STEP) && !key.startsWith(RuleConstant.SELECTED_RULE) ? RuleConstant.PREFIX.GLOBAL + key : key;
    }

    private boolean isList(String value) {
        return !value.isEmpty() && value.charAt(0) == '[';
    }

    @Override
    public boolean beforeEvaluate(Rule rule, Facts facts) {
        return selectedRuleName.equals(rule.getName());
    }

    @Override
    public void afterEvaluate(Rule rule, Facts facts, boolean evaluationResult) {

    }

    @Override
    public void beforeExecute(Rule rule, Facts facts) {

    }

    @Override
    public void onSuccess(Rule rule, Facts facts) {

    }

    @Override
    public void onFailure(Rule rule, Facts facts, Exception exception) {

    }

}
