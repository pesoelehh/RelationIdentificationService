from fastapi import FastAPI

from app.nlp_method.model.request_model import SimilarityRequest
from app.nlp_method.nlp_main import *
from fastapi.concurrency import run_in_threadpool
from app.nlp_method.nlp_main import cosine_similarity

from app.llm_method.llm_main import LLMMethod
from app.llm_method.models.request_model import ComparisonRequest
import time

# ===========================
# FastAPI app
# ===========================
app = FastAPI()


# ---------------------------
# Similarities endpoints
# ---------------------------
@app.post("/similarities/one-to-many")
async def get_similarities_one_to_many(data: SimilarityRequest):
    if not data.text1:
        return {"similarities": []}

    scores = await run_in_threadpool(cosine_similarity, data, [data.text1])
    return {"similarities": scores}


@app.post("/similarities/many-to-many")
async def get_similarities_many_to_many(data: SimilarityRequest):
    if not data.text1_list:
        return {"similarities": []}

    scores = await run_in_threadpool(cosine_similarity, data, data.text1_list)
    return {"similarities": scores}


# ---------------------------
# NLP relation endpoints
# ---------------------------
@app.post("/relationUsingNlp/one-to-many")
async def relation_using_nlp_one_to_many(data: SimilarityRequest):
    if not data.text1:
        return {"results": []}

    results = await nlp_relation_score_one_to_many(data)
    return results


@app.post("/relationUsingNlp/many-to-many")
async def relation_using_nlp_many_to_many(data: SimilarityRequest):
    if not data.text1_list:
        return {"results": []}

    results = await nlp_relation_score_many_to_many(data)
    return results


# -------------------------
# Endpoint for LLM-Methods
# --------------------------
llm_method = LLMMethod()

@app.post("/compare/oneToMany")
def compare_one_to_many(request: ComparisonRequest):
    start = time.time()

    results = llm_method.compare_one_to_many(request)

    end = time.time()
    print(f"Python processing time: {(end - start):.2f} seconds")
    return results


@app.post("/compare/manyToMany")
def compare_many_to_many(request: ComparisonRequest):
    start = time.time()

    results = llm_method.compare_many_to_many(request)

    end = time.time()
    print(f"Python processing time: {(end - start):.2f} seconds")

    return results