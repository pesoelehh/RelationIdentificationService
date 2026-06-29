"""
FastAPI endpoints for NLP relation analysis.

Provides one-to-many and many-to-many relation scoring between texts using
the application's NLP engine. Request validation is handled through the
SimilarityRequest model, and results are returned as structured JSON responses.

Author: Gabriel Nathanael da Gomez
Email: gabrieldagomez@gmail.com
"""


from fastapi import FastAPI

from app.nlp_method.model.request_model import SimilarityRequest
from app.nlp_method.nlp_main import *
from fastapi.concurrency import run_in_threadpool
import time

# Initialize the FastAPI application instance.
app = FastAPI()


# NLP relation endpoints
@app.post("/relationUsingNlp/one-to-many")
async def relation_using_nlp_one_to_many(data: SimilarityRequest):
    """
       Perform NLP-based relation analysis between a single source text
       and multiple target texts.

       Args:
           data (SimilarityRequest):
               Request payload containing source and target texts.

       Returns:
           dict:
               Relation analysis results.
   """
    if not data.text1:
        return {"results": []}

    results = await nlp_relation_score_one_to_many(data)
    return results


@app.post("/relationUsingNlp/many-to-many")
async def relation_using_nlp_many_to_many(data: SimilarityRequest):
    """
       Perform NLP-based relation analysis between multiple source texts
       and multiple target texts.

       Args:
           data (SimilarityRequest):
               Request payload containing source and target text lists.

       Returns:
           dict:
               Relation analysis results.
   """
    if not data.text1_list:
        return {"results": []}

    results = await nlp_relation_score_many_to_many(data)
    return results
