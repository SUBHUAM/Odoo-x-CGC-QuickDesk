"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Navbar from "../components/Navbar";
import AuthService from "../services/AuthService";

export default function CreateTicketPage() {
  const router = useRouter();
  const [form, setForm] = useState({
    subject: "",
    description: "",
    category: "TECHNICAL_SUPPORT",
    file: null,
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  // Dropdown options
  const categories = [
    "TECHNICAL_SUPPORT",
    "BILLING",
    "ACCOUNT_ISSUE",
    "FEATURE_REQUEST",
    "BUG_REPORT",
    "GENERAL_INQUIRY",
    "COMPLAINT",
  ];

  // --- Fetch current user ---
  useEffect(() => {
    const userData = AuthService.getSessionUser();
    if (!userData) {
      router.push("/login");
    }
  }, [router]);

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

    if (!form.subject || !form.description) {
      return setError("Subject and Description are required.");
    }

    try {
      setLoading(true);

      // If file handling needed, convert to URL or upload file first
      const attachmentUrl = form.file
        ? URL.createObjectURL(form.file) // Replace with upload logic
        : "";

      const ticketPayload = {
        subject: form.subject,
        description: form.description,
        category: form.category,
        attachmentUrl: attachmentUrl,
      };

      await AuthService.addTicket(ticketPayload);

      router.push("/dashboard");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar profile="1" dashboard="1" />

      <div className="max-w-lg mx-auto bg-white shadow p-6 mt-6 rounded">
        <h2 className="text-2xl font-bold mb-4">Ask Your Question</h2>

        {error && (
          <p className="text-red-500 text-sm bg-red-100 p-2 rounded mb-3">
            {error}
          </p>
        )}

        <form onSubmit={handleSubmit}>
          {/* Subject */}
          <div className="mb-3">
            <input
              type="text"
              name="subject"
              placeholder="Subject"
              value={form.subject}
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

          {/* Category Dropdown */}
          <div className="mb-3">
            <select
              name="category"
              value={form.category}
              onChange={handleChange}
              className="w-full border p-2 rounded focus:ring-2 focus:ring-blue-500"
            >
              {categories.map((cat) => (
                <option key={cat} value={cat}>
                  {cat.replace("_", " ")}
                </option>
              ))}
            </select>
          </div>

          {/* File Upload
          <div className="mb-4">
            <input
              type="file"
              name="file"
              onChange={handleChange}
              className="w-full text-sm"
            />
          </div> */}

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
