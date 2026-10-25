document.addEventListener("DOMContentLoaded", () => {
    if (window.lucide) {
        window.lucide.createIcons();
    }

    document.querySelectorAll("[data-password-toggle]").forEach((button) => {
        button.addEventListener("click", () => {
            const input = button.closest(".password-shell").querySelector("[data-password-input]");
            const visible = input.type === "text";
            input.type = visible ? "password" : "text";
            button.setAttribute("aria-label", visible ? "显示密码" : "隐藏密码");
            button.innerHTML = `<i data-lucide="${visible ? "eye" : "eye-off"}"></i>`;
            if (window.lucide) window.lucide.createIcons();
        });
    });

    document.querySelectorAll(".flash").forEach((flash) => {
        window.setTimeout(() => {
            flash.classList.add("is-hiding");
            window.setTimeout(() => flash.remove(), 220);
        }, 4200);
    });

    const coverInput = document.querySelector("[data-cover-url]");
    const coverPreview = document.querySelector("[data-cover-preview]");
    if (coverInput && coverPreview) {
        coverInput.addEventListener("blur", () => {
            coverPreview.src = coverInput.value.trim() || "/images/covers/default-book.jpg";
        });
    }

    const deleteDialog = document.querySelector("#deleteBookDialog");
    const deleteForm = document.querySelector("#deleteBookForm");
    const deleteTitle = document.querySelector("#deleteBookTitle");
    document.querySelectorAll("[data-delete-book]").forEach((button) => {
        button.addEventListener("click", () => {
            deleteForm.action = `/librarian/books/${button.dataset.id}/delete`;
            deleteTitle.textContent = button.dataset.title;
            deleteDialog.showModal();
        });
    });
    if (deleteDialog) {
        deleteDialog.querySelector("[data-dialog-close]")?.addEventListener("click", () => {
            deleteDialog.close();
        });
        deleteDialog.addEventListener("click", (event) => {
            if (event.target === deleteDialog) deleteDialog.close();
        });
    }
});
