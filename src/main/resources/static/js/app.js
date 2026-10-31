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
        }, 4500);
    });

    document.querySelectorAll(".quantity-control").forEach((control) => {
        const input = control.querySelector("[data-quantity-input]");
        if (!input) return;
        const min = Number(input.min || 1);
        const max = Number(input.max || 99);

        control.querySelector("[data-quantity-minus]")?.addEventListener("click", () => {
            input.value = String(Math.max(min, Number(input.value || min) - 1));
        });
        control.querySelector("[data-quantity-plus]")?.addEventListener("click", () => {
            input.value = String(Math.min(max, Number(input.value || min) + 1));
        });
        input.addEventListener("change", () => {
            const value = Number(input.value || min);
            input.value = String(Math.min(max, Math.max(min, value)));
        });
    });

    const coverInput = document.querySelector("[data-cover-url]");
    const coverPreview = document.querySelector("[data-cover-preview]");
    if (coverInput && coverPreview) {
        coverInput.addEventListener("blur", () => {
            coverPreview.src = coverInput.value.trim() || "/images/covers/default-book.jpg";
        });
    }

    document.querySelectorAll("form[data-confirm]").forEach((form) => {
        form.addEventListener("submit", (event) => {
            if (!window.confirm(form.dataset.confirm)) {
                event.preventDefault();
            }
        });
    });
});
