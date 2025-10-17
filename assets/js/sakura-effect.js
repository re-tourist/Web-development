// Sakura falling effect (decoupled module)
// Usage: const eff = initSakura({ count, zIndex, speed }); eff.stop();
(function () {
  function createCanvas(zIndex) {
    const cvs = document.createElement('canvas');
    cvs.id = 'sakura-canvas';
    Object.assign(cvs.style, {
      position: 'fixed',
      left: '0',
      top: '0',
      width: '100vw',
      height: '100vh',
      pointerEvents: 'none',
      zIndex: String(zIndex ?? 1),
    });
    document.body.appendChild(cvs);
    return cvs;
  }

  function Petal(w, h, opts) {
    const sizeBase = opts.sizeBase || 12;
    const sizeVar = opts.sizeVar || 10;
    this.reset = function () {
      this.x = Math.random() * w;
      this.y = -Math.random() * h;
      this.size = sizeBase + Math.random() * sizeVar;
      this.alpha = 0.6 + Math.random() * 0.4;
      this.speedY = (opts.speedY || 0.6) + Math.random() * (opts.speedYVar || 0.8);
      this.speedX = (Math.random() - 0.5) * (opts.speedXVar || 0.6);
      this.rot = Math.random() * Math.PI * 2;
      this.rotSpeed = (Math.random() - 0.5) * (opts.rotSpeedVar || 0.02);
    };
    this.reset();
    this.update = function () {
      this.y += this.speedY;
      this.x += this.speedX;
      this.rot += this.rotSpeed;
      if (this.y > h + this.size || this.x < -50 || this.x > w + 50) this.reset();
    };
    this.draw = function (ctx) {
      ctx.save();
      ctx.globalAlpha = this.alpha;
      ctx.translate(this.x, this.y);
      ctx.rotate(this.rot);
      // Draw a simple petal using a rotated ellipse with a notch
      const s = this.size;
      const grad = ctx.createRadialGradient(0, 0, 0, 0, 0, s);
      grad.addColorStop(0, 'rgba(255,182,193,0.95)'); // light pink
      grad.addColorStop(1, 'rgba(255,105,180,0.55)'); // hot pink
      ctx.fillStyle = grad;
      ctx.beginPath();
      ctx.scale(1.2, 0.9);
      ctx.moveTo(0, -s);
      ctx.quadraticCurveTo(s * 0.9, -s * 0.3, 0, s);
      ctx.quadraticCurveTo(-s * 0.9, -s * 0.3, 0, -s);
      ctx.fill();
      ctx.restore();
    };
  }

  function SakuraEffect(options) {
    const opts = options || {};
    let canvas = null;
    let ctx = null;
    let petals = [];
    let rafId = null;
    let w = 0, h = 0;

    function resize() {
      w = window.innerWidth; h = window.innerHeight;
      canvas.width = w; canvas.height = h;
    }

    function loop() {
      ctx.clearRect(0, 0, w, h);
      for (let i = 0; i < petals.length; i++) {
        const p = petals[i];
        p.update();
        p.draw(ctx);
      }
      rafId = requestAnimationFrame(loop);
    }

    this.start = function () {
      if (canvas) return;
      canvas = createCanvas(opts.zIndex ?? 1);
      ctx = canvas.getContext('2d');
      resize();
      const count = Math.max(12, opts.count || 30);
      petals = new Array(count).fill(0).map(() => new Petal(w, h, opts));
      window.addEventListener('resize', resize);
      rafId = requestAnimationFrame(loop);
    };

    this.stop = function () {
      if (!canvas) return;
      cancelAnimationFrame(rafId);
      window.removeEventListener('resize', resize);
      canvas.remove();
      canvas = null; ctx = null; petals = []; rafId = null;
    };
  }

  // Public API
  window.initSakura = function (options) {
    const eff = new SakuraEffect(options);
    eff.start();
    return eff;
  };
})();