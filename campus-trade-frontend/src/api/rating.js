import request from "./request";
export const createRating = (data) => request.post("/rating", data);
export const getSellerRatings = (sellerId) => request.get("/rating/seller/" + sellerId);