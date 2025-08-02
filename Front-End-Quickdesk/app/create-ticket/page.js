"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Navbar from "../components/Navbar";
import AuthService from "../services/AuthService";

export default function CreateTicketPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    question: "",
    description: "",
    tags: "",
    file: null,
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value, files } = e.target;
    if (name === "file") {
      setForm({ ...form, file: files[0] });
    } else {
      setForm({ ...form, [name]: value });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (!form.question || !form.description) {
      return setError("Question and Description are required.");
    }

    try {
      setLoading(true);

      // Convert to form-data for file upload
      const formData = new FormData();
      formData.append("question", form.question);
      formData.append("description", form.description);
      formData.append("tags", form.tags);
      if (form.file) formData.append("file", form.file);

      // Upload using AuthService
      await AuthService.getCsrfToken();
      const response = await fetch("http://localhost:8080/api/tickets", {
        method: "POST",
        credentials: "include",
        headers: {
          "X-CSRF-TOKEN": authService.csrfToken,
        },
        body: formData,
      });

      if (!response.ok) throw new Error("Failed to create ticket");

      router.push("/dashboard");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />

      <div className="max-w-lg mx-auto bg-white shadow p-6 mt-6 rounded">
        <h2 className="text-2xl font-bold mb-4">Ask Your Question</h2>

        {error && (
          <p className="text-red-500 text-sm bg-red-100 p-2 rounded mb-3">
            {error}
          </p>
        )}

        <form onSubmit={handleSubmit}>
          {/* Question */}
          <div className="mb-3">
            <input
              type="text"
              name="question"
              placeholder="Question"
              value={form.question}
              onChange={handleChange}
              className="w-full border p-2 rounded focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* Description */}
          <div className="mb-3">
            <textarea
              name="description"
              placeholder="Description"
              value={form.description}
              onChange={handleChange}
              className="w-full border p-2 rounded focus:ring-2 focus:ring-blue-500"
              rows={4}
            ></textarea>
          </div>

          {/* Tags */}
          <div className="mb-3">
            <input
              type="text"
              name="tags"
              placeholder="Tags (comma separated)"
              value={form.tags}
              onChange={handleChange}
              className="w-full border p-2 rounded focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* File Upload */}
          <div className="mb-4">
            <input
              type="file"
              name="file"
              onChange={handleChange}
              className="w-full text-sm"
            />
          </div>

          {/* Post Button */}
          <button
            type="submit"
            disabled={loading}
            className={`w-full py-2 text-white rounded ${
              loading
                ? "bg-blue-300 cursor-not-allowed"
                : "bg-blue-500 hover:bg-blue-600"
            }`}
          >
            {loading ? "Posting..." : "Post"}
          </button>
        </form>
      </div>
    </div>
  );
}

