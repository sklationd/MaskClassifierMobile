import torch
torchmodel = torch.jit.load("best.pt", map_location='cpu')
torch.jit.save(torchmodel, "best_cpu.pt")
